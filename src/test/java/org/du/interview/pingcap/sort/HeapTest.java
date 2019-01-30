package org.du.interview.pingcap.sort;

import org.junit.Test;

import java.nio.ByteBuffer;

public class HeapTest {

    @Test
    public void test(){

        long[] keys = new long[]{50L, 10L, 90L, 30L, 70L, 40L, 80L, 60L, 20L};
        ByteBuffer bf = ByteBuffer.allocate(keys.length * 16);

        for (long key : keys) {
            bf.putLong(key);
            bf.putLong(key - 3);
        }
        bf.clear();

        Heap heap = new Heap(bf, 16, (l1, l2) -> Long.compare(l1, l2));
        System.out.println(heap);

        heap.setRoot(oneRecord(5L));

        System.out.println(heap);

        heap.setRoot(oneRecord(25L));

        System.out.println(heap);

        heap.setRoot(oneRecord(35L));

        System.out.println(heap);
    }

    private ByteBuffer oneRecord(long value){
        ByteBuffer bf = ByteBuffer.allocate(16);
        bf.putLong(value);
        bf.putLong(value - 3);
        bf.flip();
        return bf;
    }


}
