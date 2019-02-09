package org.du.interview.pingcap.sort;

import org.du.interview.pingcap.util.BigByteBuffer;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 基于ByteBuffer实现的堆结构
 */
public class Heap {

    private BigByteBuffer content;

    //一条记录的字节数,int类型,虽然这里给了一个long
    private final long recordLen;

    //堆的总大小
    private long capacity;

    private LongComparator comparator;

    private long originCapacity;

    public Heap(BigByteBuffer content, int recordLen, LongComparator comparator) {
        this.content = content;
        this.comparator = comparator;
        this.recordLen = recordLen;
        this.capacity = content.getSize() / recordLen;
        this.originCapacity = capacity;
        init();
    }

    public void reorder() {
        this.capacity = this.originCapacity;
        init();
    }

    private long getRecordLongKey(long index) {
        return content.getLong(index * recordLen);
    }

    private ByteBuffer getRecordBuffer(long index) {
        long start = index * recordLen;
        return content.wrapInByteBuffer(start, (int) recordLen);
    }

    private byte[] getRecordBytes(long index) {
        long start = index * recordLen;
        return content.getBytes(start, (int) recordLen);
    }

    private void setRecord(long index, ByteBuffer record) {
        content.setByteBuffer(index * recordLen, record);
    }

    private void setRecord(long index, byte[] record) {
        content.setBytes(index * recordLen, record);
    }

    private void heapAdjust(long parent) {
        long originParent = parent;
        long temp = getRecordLongKey(parent);
        byte[] tempBytes = getRecordBytes(parent);

        long child = 2L * parent + 1L; // 先获得左孩子

        while (child < capacity) {
            long childKey = getRecordLongKey(child);
            long rightKey;
            if (child + 1L < capacity &&
                    comparator.compare(childKey,
                            (rightKey = getRecordLongKey(child + 1L))) > 0) {
                child++;
                childKey = rightKey;
            }

            if (comparator.compare(temp, childKey) <= 0) {
                break;
            }

            setRecord(parent, getRecordBuffer(child));

            parent = child;
            child = 2 * child + 1;
        }

        if (parent != originParent) {
            setRecord(parent, tempBytes);
        }
    }

    public void setRoot(ByteBuffer root) {
        content.setByteBuffer(0, root);

        heapAdjust(0);
    }

    /**
     * 得到堆的根节点
     *
     * @return
     */
    public ByteBuffer getRoot() {
        return content.wrapInByteBuffer(0, (int) recordLen);
    }

    private void init() {
        if (capacity == 0) {
            return;
        }
        for (long i = (capacity - 1) / 2; i >= 0; i--) {
            heapAdjust(i);
        }
    }

    /**
     * 按序消费堆中元素
     *
     * @param consumer
     * @return 剩余的无法在此轮进行排序的元素
     */
    public BigByteBuffer forEach(Consumer<ByteBuffer> consumer) {

        BigByteBuffer rest = null;

        if (capacity != originCapacity){
            long originalSize = originCapacity * recordLen;
            long currSize = capacity * recordLen;
            rest = content.slice(currSize, originalSize - currSize);
        }



        if (capacity != 0) {
            consumer.accept(getRoot());
        }

        while (shrink(null) != 0) {
            consumer.accept(getRoot());
        }

        return rest;
    }

    public long shrink(ByteBuffer newRecord) {
        if (capacity == 0) {
            return 0;
        }

        capacity -= 1;

        //把最后一个元素赋给第一个元素,因为第一个元素(根元素)刚刚已经被写入写缓冲了
        setRecord(0, getRecordBuffer(capacity));
        heapAdjust(0);

        if (newRecord != null) {
            //将新记录给最后一个元素
            setRecord(capacity, newRecord);
        }
        return capacity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int i = 0; i < capacity; i++) {
            ByteBuffer byteBuffer = getRecordBuffer(i);
            sb.append("[");
            sb.append(byteBuffer.getLong())
                    .append(",");
            sb.append(byteBuffer.getLong());
            sb.append("],");
        }
/*        long pos = 0;
        for (int i = 0; i < capacity; i++) {
            sb.append("[");
            sb.append(content.getLong(pos))
                    .append(",");
            pos += 8;
            sb.append(content.getLong(pos));
            pos += 8;
            sb.append("],");
        }*/
        sb.append("]");

        return sb.toString();
    }
}
