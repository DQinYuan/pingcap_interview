package org.du.interview.pingcap.util;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;

import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

@SuppressWarnings("restriction")
public class MMapper {

    private static final Unsafe unsafe;
    private static final Method mmap;
    private static final Method unmmap;
    private static final int BYTE_ARRAY_OFFSET;

    private long addr, size;
    private final String loc;

    static {
        try {
            unsafe = MyUnsafe.UNSAFE;

            mmap = ReflectiveUtil.getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            unmmap = ReflectiveUtil.getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);

            //16  byte数组的初始偏移 12B对象头 + 4B的长度
            BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    //Round to next 4096 bytes
    private static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }

    //Given that the location and size have been set, map that location
    //for the given length and set this.addr to the returned offset
    private void mapAndSetOffset() throws Exception{
        final RandomAccessFile backingFile = new RandomAccessFile(this.loc, "rw");
        //backingFile.setLength(this.size);

        final FileChannel ch = backingFile.getChannel();
        this.addr = (long) mmap.invoke(ch, 1, 0L, this.size);

        ch.close();
        backingFile.close();
    }

    public MMapper(final String loc, int len) throws Exception {
        this.loc = loc;
        this.size = roundTo4096(len);
        mapAndSetOffset();
    }

    //需要进行同步,这里
    public void unmap() throws Exception{
        unmmap.invoke(null, addr, this.size);
    }

    public int getInt(int pos){
        return unsafe.getInt(pos + addr);
    }

    public long getLong(int pos){
        return unsafe.getLong(pos + addr);
    }

    public byte getByte(int pos){
        return unsafe.getByte(pos + addr);
    }

    public void putInt(int pos, int val){
        unsafe.putInt(pos + addr, val);
    }

    public void putLong(int pos, long val){
        unsafe.putLong(pos + addr, val);
    }

    //May want to have offset & length within data as well, for both of these
    public void getBytes(int pos, byte[] data){
        unsafe.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET, data.length);
    }

    public void setBytes(int pos, byte[] data){
        unsafe.copyMemory(data, BYTE_ARRAY_OFFSET, null, pos + addr, data.length);
    }
}
