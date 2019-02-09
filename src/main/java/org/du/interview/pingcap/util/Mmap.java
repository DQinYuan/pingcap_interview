package org.du.interview.pingcap.util;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Mmap {

    private static final Unsafe unsafe;
    private static final Method mmap;
    private static final Method unmmap;

    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;

    static {
        try {
            unsafe = MyUnsafe.UNSAFE;

            mmap = ReflectiveUtil.getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            unmmap = ReflectiveUtil.getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Path loc;
    private long size;
    private long offset;
    //addr是逻辑的开始地址
    private long addr;

    //必须使用realAddr进行unmap
    private long realAddr;
    private FileChannel.MapMode mode;

    private FileChannel channel;

    /**
     * @param loc
     * @param offset
     * @param len
     */
    public Mmap(final Path loc, long offset, int len, FileChannel.MapMode mode) {
        this.loc = loc;
        this.size = len;
        this.offset = offset;
        this.mode = mode;
        mapAndSetOffset();
    }

    public Mmap(final FileChannel channel, long offset, int len, FileChannel.MapMode mode) {
        this.size = len;
        this.offset = offset;
        this.mode = mode;
        this.channel = channel;
        mapAndSetOffset();
    }

    public Mmap(final Path loc, final FileChannel channel, long offset, int len, FileChannel.MapMode mode) {
        this.loc = loc;
        this.size = len;
        this.offset = offset;
        this.mode = mode;
        mapAndSetOffset();
    }

    public long getSize() {
        return size;
    }

    //Given that the location and size have been set, map that location
    //for the given length and set this.addr to the returned offset
    private void mapAndSetOffset() {
        int imode = -1;

        //处理操作码
        if (mode == FileChannel.MapMode.READ_ONLY)
            imode = MAP_RO;
        else if (mode == FileChannel.MapMode.READ_WRITE)
            imode = MAP_RW;
        else if (mode == FileChannel.MapMode.PRIVATE)
            imode = MAP_PV;

        //在内存页内的偏移
        int pagePosition = (int) (offset % Constant.allocationGranularity);
        //页偏移(页大小的整数倍)
        long mapPosition = offset - pagePosition;
        //用户指定映射大小 + 页内偏移
        long mapSize = size + pagePosition;

        try {
            this.channel = channel == null ? FileChannel.open(this.loc, StandardOpenOption.WRITE,
                    StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING) : channel;
            this.realAddr = (long) mmap.invoke(channel, imode, mapPosition,
                    mapSize);

            this.addr = this.realAddr + pagePosition;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //需要进行同步,这里
    public void unmap(boolean close) {
        try {
            unmmap.invoke(null, realAddr, this.size);
            if (close) {
                this.channel.close();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //注意屏蔽操作系统大小端差异
    public long getLong(int pos) {
        long nativeLong = unsafe.getLong(pos + addr);
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
                nativeLong :
                Long.reverseBytes(nativeLong);
    }

    //注意屏蔽操作系统大小端差异
    public long setLong(int pos, long value) {
        return unsafe.getAndSetLong(null, pos + addr,
                ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
                        value : Long.reverseBytes(value));
    }

    public byte getByte(int pos) {
        return unsafe.getByte(pos + addr);
    }


    public void setByte(int pos, byte value){
        setBytes(new byte[]{value}, pos);
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

}
