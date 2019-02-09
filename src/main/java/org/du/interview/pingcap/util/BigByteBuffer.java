package org.du.interview.pingcap.util;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BigByteBuffer {

    private FastDirectByteBuffer[] byteBuffers;
    private long size;

    private BigByteBuffer() {

    }

    public BigByteBuffer(long size) {
        this.size = size;
        int num = (int) (size / Constant.G);
        int rest = (int) (size % Constant.G);
        num = rest == 0 ? num : num + 1;

        this.byteBuffers = new FastDirectByteBuffer[num];

        for (int i = 0; i < byteBuffers.length - 1; i++) {
            this.byteBuffers[i] = new FastDirectByteBuffer((int) Constant.G);
        }

        this.byteBuffers[byteBuffers.length - 1] = new FastDirectByteBuffer(
                rest == 0 ? (int) Constant.G : rest
        );
    }

    /**
     * 逻辑位置pos位于的block编号
     *
     * @param pos
     * @return
     */
    protected int block(long pos) {
        return (int) (pos / Constant.G);
    }

    /**
     * 逻辑位置pos在block内的偏移
     *
     * @param pos
     * @return
     */
    protected int offset(long pos) {
        return (int) (pos % Constant.G);
    }

    public long getLong(long pos) {
        return byteBuffers[block(pos)].getLong(offset(pos));
    }

    public byte getByte(long pos) {
        return byteBuffers[block(pos)].getByte(offset(pos));
    }

    public long setLong(long pos, long value) {
        FastDirectByteBuffer blockBuffer = byteBuffers[block(pos)];
        return blockBuffer.setLong(offset(pos), value);
    }


    public byte[] getBytes(byte[] dst, long pos, int len) {
        long start = pos;
        long end = start + len - 1;

        int blockStart = block(start);
        int offsetStart = offset(start);
        int blockEnd = block(end);
        int offsetEnd = offset(end);

        //位于同block的情况
        if (blockStart == blockEnd) {
            return byteBuffers[blockStart].getBytes(dst, 0, offsetStart, len);
        }

        //位于不同block的情况
        int byteStart = 0;

        int startLen = (int) (byteBuffers[blockStart].getSize() - offsetStart);
        byteBuffers[blockStart].getBytes(dst, byteStart, offsetStart, startLen);
        byteStart += startLen;

        for (int i = blockStart + 1; i <= blockEnd - 1; i++) {
            byteBuffers[i].getBytes(dst, byteStart, 0, (int) Constant.G);
            byteStart += Constant.G;
        }

        int endLen = offsetEnd + 1;
        byteBuffers[blockEnd].getBytes(dst, byteStart, 0, endLen);

        return dst;
    }

    public byte[] getBytes(long pos, int len) {
        byte[] dst = new byte[len];
        return getBytes(dst, pos, len);
    }

    public long getSize() {
        return size;
    }

    /**
     * 该方法不会移动文件指针
     *
     * @param channel
     * @param filePosition
     */
    public void fillFromFileChannel(FileChannel channel, long filePosition) {
        for (FastDirectByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.fillFromFileChannel(channel, filePosition);
            filePosition += byteBuffer.getSize();
        }
    }

    /**
     * 该方法会移动文件指针
     *
     * @param channel
     */
    public void fillFromFileChannel(FileChannel channel) {
        for (FastDirectByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.fillFromFileChannel(channel);
        }
    }

    /**
     * 支持跨block
     *
     * @param pos
     * @param src
     */
    public void setBytes(long pos, byte[] src) {
        long start = pos;
        long end = start + src.length - 1;

        int blockStart = block(start);
        int offsetStart = offset(start);
        int blockEnd = block(end);
        int offsetEnd = offset(end);

        //位于同block的情况
        if (blockStart == blockEnd) {
            byteBuffers[blockStart].setBytes(src, 0, offsetStart, src.length);
            return;
        }

        //位于不同block的情况
        int byteStart = 0;

        int startLen = (int) (byteBuffers[blockStart].getSize() - offsetStart);
        byteBuffers[blockStart].setBytes(src, byteStart, offsetStart, startLen);
        byteStart += startLen;

        for (int i = blockStart + 1; i <= blockEnd - 1; i++) {
            byteBuffers[i].setBytes(src, byteStart, 0, (int) Constant.G);
            byteStart += Constant.G;
        }

        int endLen = offsetEnd + 1;
        byteBuffers[blockEnd].setBytes(src, byteStart, 0, endLen);
    }

    /**
     * 暂时实现得不可以跨block,未来会支持
     *
     * @param pos
     * @param directBuffer
     */
    public void setByteBuffer(long pos, ByteBuffer directBuffer) {
        int block = chectNotCross(pos, directBuffer.remaining());
        int offset = offset(pos);

        this.byteBuffers[block].setByteBuffer(offset, directBuffer);
    }

    /**
     * 将一定范围的数据包装进一个ByteBuffer中,不支持跨块
     *
     * @param pos
     * @param len
     * @return
     */
    public ByteBuffer wrapInByteBuffer(long pos, int len) {
        int blockStart = chectNotCross(pos, len);
        int offsetStart = offset(pos);

        return byteBuffers[blockStart].wrapInByteBuffer(offsetStart, len);
    }


    /**
     * 校验该范围查询没有跨块
     *
     * @param start
     * @param len
     * @return 范围查询数据位于的block编号
     */
    private int chectNotCross(long start, int len) {
        long end = start + len - 1;

        int blockStart = block(start);
        int blockEnd = block(end);
        if (blockStart != blockEnd) {
            throw new RuntimeException("该操作不允许跨block");
        }

        return blockStart;
    }

    public BigByteBuffer slice(long pos, long len) {
        if (pos >= size){
            throw new RuntimeException("pos不允许超过bigmmap的长度");
        }
        return new BigByteBufferView(pos, len);
    }

    public void free() {
        for (FastDirectByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.free();
        }
    }


    private class BigByteBufferView extends BigByteBuffer {

        private long firstBlockLen;


        public BigByteBufferView(long start, long len) {
            super();
            super.size = len;

            FastDirectByteBuffer[] superByteBuffers = BigByteBuffer.this.byteBuffers;

            long end = start + len - 1;
            int blockStart = (int) (start / Constant.G);
            int offsetStart = (int) (start % Constant.G);
            int blockEnd = (int) (end / Constant.G);
            int offsetEnd = (int) (end % Constant.G);

            int blockNum = blockEnd - blockStart + 1;
            FastDirectByteBuffer[] byteBuffers
                    = super.byteBuffers = new FastDirectByteBuffer[blockNum];

            FastDirectByteBuffer firstBuffer = superByteBuffers[blockStart];
            this.firstBlockLen = firstBuffer.getSize() - offsetStart;
            byteBuffers[0] = firstBuffer.slice(offsetStart, (int) firstBlockLen);

            int i = blockStart + 1;
            for (; i <= blockEnd - 1; i++) {
                byteBuffers[i - blockStart] = superByteBuffers[i];
            }

            if (blockEnd != blockStart) {
                FastDirectByteBuffer lastBuffer = superByteBuffers[blockEnd];
                byteBuffers[i - blockStart] = lastBuffer.slice(0, offsetEnd + 1);
            }
        }

        @Override
        protected int block(long pos) {
            if (pos < firstBlockLen) {
                return 0;
            }
            return super.block(pos - firstBlockLen) + 1;
        }

        @Override
        protected int offset(long pos) {
            if (pos < firstBlockLen) {
                return (int) pos;
            }
            return super.offset(pos - firstBlockLen);
        }
    }


}
