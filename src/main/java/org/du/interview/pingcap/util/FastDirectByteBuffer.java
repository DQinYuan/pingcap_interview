package org.du.interview.pingcap.util;

import sun.nio.ch.DirectBuffer;
import sun.nio.ch.FileChannelImpl;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class FastDirectByteBuffer {

    private long addr;
    private long size;

    //用于获取FileChannel的fd成员变量
    private static final Field fd;

    //用于获取FileChannel的nd成员变量
    private static final Field nd;

    //pread方法可以从指定位置读取文件内容
    private static final Method pread;

    //read方法可以顺序读文件的内容
    private static final Method read;

    //DirectBuffer的构造方法
    private static final Constructor<?> directByteBufferCons;


    static {
        try {
            //参数含义:文件描述符,写入的内存位置,写入大小,文件位置
            pread = ReflectiveUtil.getMethod(Class.forName("sun.nio.ch.FileDispatcherImpl"),
                    "pread", Class.forName("java.io.FileDescriptor"), long.class,
                    int.class, long.class);
            read = ReflectiveUtil.getMethod(Class.forName("sun.nio.ch.FileDispatcherImpl"),
                    "read", Class.forName("java.io.FileDescriptor"), long.class,
                    int.class);
            fd = ReflectiveUtil.getField(FileChannelImpl.class, "fd");
            nd = ReflectiveUtil.getField(FileChannelImpl.class, "nd");
            directByteBufferCons = ReflectiveUtil.getConstructor(Class.forName("java.nio.DirectByteBuffer"),
                    long.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public FastDirectByteBuffer(long addr, int size){
        this.addr = addr;
        this.size = size;
    }

    public FastDirectByteBuffer(int size) {
        this.addr = MyUnsafe.UNSAFE.allocateMemory(size);
        this.size = size;
    }

    public long getLong(int pos) {
        long origin = MyUnsafe.UNSAFE.getLong(this.addr + pos);

        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN?
                origin:
                Long.reverseBytes(origin);
    }

    public byte getByte(int pos) {
        return MyUnsafe.UNSAFE.getByte(this.addr + pos);
    }

    public long setLong(int pos, long value) {
        return MyUnsafe.UNSAFE.getAndSetLong(null,
                this.addr + pos,
                //屏蔽操作系统大小端
                ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN?
                value:
                Long.reverseBytes(value));
    }

    public byte[] getBytes(byte[] dst, int pos, int len) {
        return getBytes(dst, 0, pos, len);
    }

    public byte[] getBytes(byte[] dst, int dstPos, int pos, int len) {
        if (dstPos + len > dst.length) {
            throw new RuntimeException("dstPos + len不能超过数组最大下标");
        }

        MyUnsafe.UNSAFE.copyMemory(null, pos + addr,
                dst, MyUnsafe.BYTE_ARRAY_BASE_OFFSET + dstPos,
                len);
        return dst;
    }

    public byte[] getBytes(int pos, int len) {
        byte[] dst = new byte[len];
        return getBytes(dst, pos, len);
    }


    public void setBytes(byte[] src, int pos){
        setBytes(src, pos, src.length);
    }

    public void setBytes(byte[] src, int pos, int len){
        setBytes(src, 0, pos, len);
    }

    public void setBytes(byte[] src, int srcPos, int pos, int len){
        MyUnsafe.UNSAFE.copyMemory(src,
                MyUnsafe.BYTE_ARRAY_BASE_OFFSET + srcPos,
                null,
                pos + addr, len);
    }

    public void setByteBuffer(int pos, ByteBuffer directBuffer){
        if ( !(directBuffer instanceof DirectBuffer) ){
            throw new RuntimeException("该方法只允许拷贝直接内存");
        }

        int bufferPos = directBuffer.position();
        int len = directBuffer.limit() - bufferPos;

        MyUnsafe.UNSAFE.copyMemory(null,
                ((DirectBuffer)directBuffer).address() + bufferPos,
                null,
                pos + this.addr,
                len);
        directBuffer.position(directBuffer.limit());
    }

    public long getSize() {
        return size;
    }

    public FastDirectByteBuffer slice(int pos, int len){
        return new FastDirectByteBuffer(pos + addr, len);
    }

    public void free() {
        MyUnsafe.UNSAFE.freeMemory(this.addr);
    }

    /**
     * 从绝对位置开始填充该buffer
     * 该方法不会移动文件指针
     * @param fileChannel
     * @param filePosition
     */
    public void fillFromFileChannel(FileChannel fileChannel, long filePosition) {
        try {
            FileDescriptor fileDesc = (FileDescriptor) fd.get(fileChannel);
            Object nativeDispatcher = nd.get(fileChannel);
            //文件描述符,写入的内存位置,写入大小,文件位置
            pread.invoke(nativeDispatcher, fileDesc, this.addr, (int) this.size, filePosition);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从fileChannel中填充该buffer
     * 该方法会移动相应长度的文件指针
     * @param fileChannel
     */
    public void  fillFromFileChannel(FileChannel fileChannel){
        try {
            FileDescriptor fileDesc = (FileDescriptor) fd.get(fileChannel);
            Object nativeDispatcher = nd.get(fileChannel);
            read.invoke(nativeDispatcher, fileDesc, this.addr, (int)this.size);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    public ByteBuffer wrapInByteBuffer(){
        try {
            return (ByteBuffer) directByteBufferCons.newInstance(this.addr, (int)this.size);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer wrapInByteBuffer(int pos, int len){
        try {
            ByteBuffer byteBuffer = (ByteBuffer) directByteBufferCons.newInstance(this.addr + pos,
                    len);
            return byteBuffer;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
