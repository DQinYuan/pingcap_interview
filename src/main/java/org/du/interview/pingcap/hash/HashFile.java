package org.du.interview.pingcap.hash;

import org.du.interview.pingcap.util.BigMmap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class HashFile {

    private final BigMmap mmap;

    private final long capacity;

    //每条记录的长度
    private final long recordLen;
    //包含标志位的每条记录长度
    private final long realLen;

    private static final double LOAD_FACTOR = 0.75;

    /**
     *
     * @param path
     * @param expectSize  期望存放的记录数目
     * @param recordLen
     */
    public HashFile(Path path, long expectSize, int recordLen) {
        try {
            if (Files.exists(path)){
                Files.delete(path);
            }

            this.capacity = (long) (expectSize / LOAD_FACTOR);
            this.recordLen = recordLen;
            this.realLen = recordLen + 1;

            RandomAccessFile raf= new RandomAccessFile(path.toFile(), "rw");
            raf.setLength(this.capacity * realLen);

            FileChannel channel = raf.getChannel();
            this.mmap = new BigMmap(channel, FileChannel.MapMode.READ_WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long inc(long i) {
        return ++i == capacity ? 0 : i;
    }

    private boolean exists(long pos) {
        byte flag = mmap.getByte(pos * realLen);
        return flag == 1;
    }

    public void put(long key, long value) {
        long index = key % capacity;
        while (exists(index)) {
            index = inc(index);
        }

        long start = index * realLen;
        mmap.setByte(start, (byte) 1);
        mmap.setLong(start + 1, key);
        mmap.setLong(start + 9, value);
    }

    private long getKey(long index){
        return mmap.getLong(index * realLen + 1);
    }

    private long getValue(long index){
        return mmap.getLong(index * realLen + 9);
    }


    /**
     * 这里的实现默认key是一定存在的
     * @param key
     * @return
     */
    public long get(long key){
        long index = key % capacity;
        while (getKey(index) != key){
            index = inc(index);
        }

        return getValue(index);
    }


}
