package org.du.interview.pingcap.sort;

import org.du.interview.pingcap.Config;
import org.du.interview.pingcap.util.BigByteBuffer;
import org.du.interview.pingcap.util.EPathUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ESort {


    public static void sort(Path in, Path out, Path temp, int recordLen, LongComparator longComparator) {
        //初始化run
        initRun(in, temp, recordLen, longComparator);

        //多路归并
        multiwayMerge(temp, out, recordLen);

    }

    public static void initRun(Path in, Path temp, int recordLen, LongComparator longComparator){
        ReadBuffer readBuffer = new ReadBuffer(in);
        int num = 0;
        WriteBuffer writeBuffer = new WriteBuffer(EPathUtil.createTmpPath(num, temp));
        BigByteBuffer content = new BigByteBuffer(Config.CONTENT_SIZE);

        //读取一个block并构建堆
        readBuffer.read(content);
        Heap heap = new Heap(content, recordLen, longComparator);

        ByteBuffer next;
        while ((next = readBuffer.nextRecord(recordLen)) != null) {
            ByteBuffer lastByteBuffer = heap.getRoot();
            //System.out.println("key:" + heap.getRoot().getLong());
            writeBuffer.write(lastByteBuffer);
            lastByteBuffer.clear();
            long lastKey = lastByteBuffer.getLong();

            long currentKey = next.getLong();
            next.clear();
            if ( longComparator.compare(currentKey, lastKey) >= 0 ){
                heap.setRoot(next);
            } else if ( heap.shrink(next) == 0 ){
                //System.out.println("file switch");
                num++;
                writeBuffer.flushAndClose();
                writeBuffer = new WriteBuffer(EPathUtil.createTmpPath(num, temp));
                heap.reorder();
            }
        }

        //处理堆中剩余元素
        BigByteBuffer rest = heap.forEach(writeBuffer::write);

        num++;
        writeBuffer.flushAndClose();
        //System.out.println("file switch");
        if (rest != null){
            writeBuffer = new WriteBuffer(EPathUtil.createTmpPath(num, temp));
            new Heap(rest, recordLen, longComparator).forEach(writeBuffer::write);
            writeBuffer.flushAndClose();
        }

        //释放内存
        content.free();
    }


    public static void multiwayMerge(Path tmpDir, Path out, int recordLen){
        List<ReadBuffer> readBuffers = EPathUtil.listTmpFile(tmpDir)
                .map(ReadBuffer::new)
                .collect(Collectors.toCollection(ArrayList::new));

        PriorityQueue<KV> kvs = new PriorityQueue<>(readBuffers.size());

        int num = 0;
        for (ReadBuffer readBuffer : readBuffers) {
            ByteBuffer byteBuffer = readBuffer.nextRecord(recordLen);
            KV kv = KV.fromByteBuffer(byteBuffer, num);
            kvs.add(kv);
            num++;
        }

        WriteBuffer wb = new WriteBuffer(out);

        while (!kvs.isEmpty()){
            KV kv = kvs.poll();
            //System.out.println(kv);
            wb.write(kv.getClearByteBuffer());

            KV nextKV = nextKV(readBuffers.get(kv.getNum()), recordLen, kv.getNum());
            if ( nextKV != null ){
                kvs.add(nextKV);
            }
        }

        wb.flushAndClose();
    }


    private static KV nextKV(ReadBuffer readBuffer, int recordLen, int num){
        ByteBuffer byteBuffer = readBuffer.nextRecord(recordLen);
        if ( byteBuffer == null ){
            return null;
        }

        return KV.fromByteBuffer(byteBuffer, num);
    }

}
