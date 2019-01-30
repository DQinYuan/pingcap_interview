package org.du.interview.pingcap.sort;

import java.nio.ByteBuffer;

public class KV implements Comparable<KV> {

    private final int num;

    private final long key;

    private final long value;

    private final ByteBuffer byteBuffer;


    public KV(int num, long key, long value, ByteBuffer byteBuffer) {
        this.num = num;
        this.key = key;
        this.value = value;
        this.byteBuffer = byteBuffer;
    }

    public static KV fromByteBuffer(ByteBuffer byteBuffer, int num){
        return new KV(num, byteBuffer.getLong(), byteBuffer.getLong(), byteBuffer);
    }

    public int getNum() {
        return num;
    }

    public long getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    public ByteBuffer getClearByteBuffer(){
        byteBuffer.clear();
        return byteBuffer;
    }

    @Override
    public int compareTo(KV o) {
        return Long.compare(this.key, o.getKey());
    }

    @Override
    public String toString() {
        return "KV{" +
                "num=" + num +
                ", key=" + key +
                ", value=" + value +
                '}';
    }
}
