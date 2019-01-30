package org.du.interview.pingcap.sort;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;

public class KVTest {

    @Ignore
    @Test
    public void priorityTest(){
        PriorityQueue<KV> kvs = new PriorityQueue<>();

        kvs.add(KV.fromByteBuffer(kv(9, 6), 0));
        kvs.add(KV.fromByteBuffer(kv(4, 1), 1));
        kvs.add(KV.fromByteBuffer(kv(17, 14), 2));

        System.out.println(kvs.poll());
        System.out.println(kvs.poll());
        System.out.println(kvs.poll());
    }


    private ByteBuffer kv(long key, long value){
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(key);
        byteBuffer.putLong(value);
        byteBuffer.flip();
        return byteBuffer;
    }

}
