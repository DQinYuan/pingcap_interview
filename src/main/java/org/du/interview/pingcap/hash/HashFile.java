package org.du.interview.pingcap.hash;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HashFile {

    private final FileChannel channel;

    private final MappedByteBuffer mmap;

    private final int capacity;

    //每条记录的长度
    private final int recordLen;
    //包含标志位的每条记录长度
    private final int realLen;

    private static final double LOAD_FACTOR = 0.75;

    public HashFile(Path path, int expectSize, int recordLen) {
        try {
            this.channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ,
                    StandardOpenOption.CREATE);
            this.capacity = (int) (expectSize / LOAD_FACTOR);
            this.recordLen = recordLen;
            this.realLen = recordLen + 1;

            this.mmap = channel.map(FileChannel.MapMode.READ_WRITE,
                    0, this.capacity * realLen);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int inc(int i) {
        return ++i == capacity ? 0 : i;
    }

    private boolean exists(int index) {
        byte flag = mmap.get(index * realLen);
        return flag == 1;
    }

    public void put(long key, long value) {
        int index = (int) (key % capacity);
        while (exists(index)) {
            index = inc(index);
        }

        int start = index * realLen;
        mmap.put(start, (byte) 1);
        mmap.putLong(start + 1, key);
        mmap.putLong(start + 9, value);
    }


    private long getKey(int index){
        return mmap.getLong(index * realLen + 1);
    }

    private long getValue(int index){
        return mmap.getLong(index * realLen + 9);
    }


    public long get(long key){
        int index = (int) (key % capacity);
        while (getKey(index) != key){
            index = inc(index);
        }

        return getValue(index);
    }


}
