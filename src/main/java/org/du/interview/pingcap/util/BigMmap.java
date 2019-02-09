package org.du.interview.pingcap.util;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 一次性把整个文件映射成mmap
 */
public class BigMmap {

    private Mmap[] mmaps;
    private long fileSize;
    private FileChannel channel;

    /**
     * 逻辑位置pos位于的block编号
     * @param pos
     * @return
     */
    protected int block(long pos){
        return (int) (pos / Constant.G);
    }

    /**
     * 逻辑位置pos在block内的偏移
     * @param pos
     * @return
     */
    protected int offset(long pos){
        return (int) (pos % Constant.G);
    }

    public BigMmap(Path path, FileChannel.MapMode mode){
        try {
            this.channel = FileChannel.open(path, StandardOpenOption.WRITE,
                    StandardOpenOption.READ, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            init(this.channel, mode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BigMmap(FileChannel channel, FileChannel.MapMode mode){
        this.channel = channel;
        init(channel, mode);
    }

    private void init(FileChannel channel, FileChannel.MapMode mode){
        try {
            this.fileSize = channel.size();

            int num = (int) (fileSize / Constant.G);
            int rest = (int) (fileSize % Constant.G);
            num = rest == 0? num: num + 1;

            this.mmaps = new Mmap[num];

            int i = 0;
            for ( ; i < mmaps.length - 1; i++ ){
                this.mmaps[i] = new Mmap(channel, i * Constant.G,
                        (int) Constant.G, mode);
            }

            this.mmaps[mmaps.length - 1] = new Mmap(channel,
                    i * Constant.G,
                    rest == 0? (int) Constant.G: rest,
                    mode
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte getByte(long pos) {
        int block = (int) (pos / Constant.G);
        int offset = (int) (pos % Constant.G);
        return mmaps[block].getByte(offset);
    }

    /**
     * 支持跨block
     * @param pos
     * @return
     */
    public long getLong(long pos){
        if ( block(pos) == block(pos + 8) ){
            int block = (int) (pos / Constant.G);
            int offset = (int) (pos % Constant.G);
            return mmaps[block].getLong(offset);
        }

        byte[] longBytes = getBytes(pos, 8);
        long nativeLong = bytes2long(longBytes);

        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN?
                nativeLong:
                Long.reverseBytes(nativeLong);
    }


    /**
     * 暂时不支持跨block
     * @param pos
     * @param value
     */
    public void setLong(long pos, long value){
        if (block(pos) == block(pos + 8)){
            int block = (int) (pos / Constant.G);
            int offset = (int) (pos % Constant.G);
            mmaps[block].setLong(offset, value);
            return;
        }

        byte[] longBytes =
                ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN?
                        long2bytes(value):
                        long2bytes(Long.reverseBytes(value));

        setBytes(pos, longBytes);
    }


    /**
     * to byte array.
     * 数组高位放long的低位 (大端)
     *
     * @param v   value.
     */
    private static byte[] long2bytes(long v) {
        byte[] b = new byte[8];
        b[7] = (byte) v;
        b[6] = (byte) (v >>> 8);
        b[5] = (byte) (v >>> 16);
        b[4] = (byte) (v >>> 24);
        b[3] = (byte) (v >>> 32);
        b[2] = (byte) (v >>> 40);
        b[1] = (byte) (v >>> 48);
        b[0] = (byte) (v >>> 56);

        return b;
    }

    /**
     * to long.
     *数组高位放long的低位(大端)
     * @param b   byte array.
     * @return long.
     */
    public static long bytes2long(byte[] b) {
        return ((b[7] & 0xFFL) << 0) +
                ((b[6] & 0xFFL) << 8) +
                ((b[5] & 0xFFL) << 16) +
                ((b[4] & 0xFFL) << 24) +
                ((b[3] & 0xFFL) << 32) +
                ((b[2] & 0xFFL) << 40) +
                ((b[1] & 0xFFL) << 48) +
                (((long) b[0]) << 56);
    }

    public void setByte(long pos, byte value){
        int block = (int) (pos / Constant.G);
        int offset = (int) (pos % Constant.G);
        mmaps[block].setByte(offset, value);
    }

    /**
     * 支持跨block
     * @param dst
     * @param pos
     * @param len
     * @return
     */
    public byte[] getBytes(byte[] dst, long pos, int len) {
        long start = pos;
        long end = start + len - 1;

        int blockStart = block(start);
        int offsetStart = offset(start);
        int blockEnd = block(end);
        int offsetEnd = offset(end);

        //位于同block的情况
        if ( blockStart == blockEnd ){
            return mmaps[blockStart].getBytes(dst, 0, offsetStart, len);
        }

        //位于不同block的情况
        int byteStart = 0;

        int startLen = (int) (mmaps[blockStart].getSize() - offsetStart);
        mmaps[blockStart].getBytes(dst, byteStart, offsetStart, startLen);
        byteStart += startLen;

        for ( int i = blockStart + 1; i <= blockEnd - 1; i++ ){
            mmaps[i].getBytes(dst, byteStart, 0, (int) Constant.G);
            byteStart += Constant.G;
        }

        int endLen = offsetEnd + 1;
        mmaps[blockEnd].getBytes(dst, byteStart, 0, endLen);

        return dst;
    }

    public byte[] getBytes(long pos, int len) {
        byte[] dst = new byte[len];
        return getBytes(dst, pos, len);
    }


    /**
     * 支持跨block
     * @param pos
     * @param src
     */
    public void setBytes(long pos, byte[] src){
        long start = pos;
        long end = start + src.length - 1;

        int blockStart = block(start);
        int offsetStart = offset(start);
        int blockEnd = block(end);
        int offsetEnd = offset(end);

        //位于同block的情况
        if ( blockStart == blockEnd ){
            mmaps[blockStart].setBytes(src, 0, offsetStart, src.length);
            return;
        }

        //位于不同block的情况
        int byteStart = 0;

        int startLen = (int) (mmaps[blockStart].getSize() - offsetStart);
        mmaps[blockStart].setBytes(src, byteStart, offsetStart, startLen);
        byteStart += startLen;

        for ( int i = blockStart + 1; i <= blockEnd - 1; i++ ){
            mmaps[i].setBytes(src, byteStart, 0, (int) Constant.G);
            byteStart += Constant.G;
        }

        int endLen = offsetEnd + 1;
        mmaps[blockEnd].setBytes(src, byteStart, 0, endLen);
    }

    //需要同步
    public void unmap(){
        for (Mmap mmap : mmaps) {
            mmap.unmap(false);
        }
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
