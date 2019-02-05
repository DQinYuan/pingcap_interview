package org.du.interview.pingcap.sort;

import org.du.interview.pingcap.util.BigByteBuffer;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HeapTest {

    @Test
    public void test(){

        long[] keys = new long[]{50L, 10L, 90L, 30L, 70L, 40L, 80L, 60L, 20L};
        BigByteBuffer bf = new BigByteBuffer(keys.length * 16);

        long pos = 0;
        for (long key : keys) {
            bf.setLong(pos, key);
            pos += 8;
            bf.setLong(pos, key - 3L);
            pos += 8;
        }

        //LITTLE_ENDIAN
        System.out.println(ByteOrder.nativeOrder());

        ByteBuffer byteBuffer = bf.wrapInByteBuffer(0, 16);
        System.out.println(byteBuffer.getLong());

        Heap heap = new Heap(bf, 16, (l1, l2) -> Long.compare(l1, l2));
        //[[10,7],[20,17],[40,37],[30,27],[70,67],[90,87],[80,77],[60,57],[50,47],]
        System.out.println(heap);

        //[[5,2],[20,17],[40,37],[30,27],[70,67],[90,87],[80,77],[60,57],[50,47],]
        heap.setRoot(oneRecord(5L));

        System.out.println(heap);

        //[[20,17],[25,22],[40,37],[30,27],[70,67],[90,87],[80,77],[60,57],[50,47],]
        heap.setRoot(oneRecord(25L));

        System.out.println(heap);

        //[[25,22],[30,27],[40,37],[35,32],[70,67],[90,87],[80,77],[60,57],[50,47],]
        heap.setRoot(oneRecord(35L));

        System.out.println(heap);
    }

    private ByteBuffer oneRecord(long value){
        ByteBuffer bf = ByteBuffer.allocateDirect(16);
        //bf.order(ByteOrder.nativeOrder());
        bf.putLong(value);
        bf.putLong(value - 3);
        bf.flip();
        return bf;
    }


}
