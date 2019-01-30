package org.du.interview.pingcap.sort;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 基于ByteBuffer实现的堆结构
 */
public class Heap {

    private ByteBuffer content;

    //一条记录的字节数
    private final int recordLen;

    //堆的总大小
    private int capacity;

    private LongComparator comparator;

    private int originCapacity;

    public Heap(ByteBuffer content, int recordLen, LongComparator comparator) {
        this.content = content;
        this.comparator = comparator;
        this.recordLen = recordLen;
        this.capacity = content.limit() / recordLen;
        this.originCapacity = capacity;
        init();
    }

    public void reorder() {
        this.capacity = this.originCapacity;
        init();
    }

    private long getRecordLongKey(int index) {
        content.clear();
        return content.getLong(index * recordLen);
    }

    private ByteBuffer getRecordBuffer(int index) {
        int start = index * recordLen;
        content.limit(start + recordLen);
        content.position(start);
        return content.slice();
    }

    private byte[] getRecordBytes(int index) {
        byte[] bytes = new byte[recordLen];
        int start = index * recordLen;
        content.position(start);
        content.limit(start + recordLen);
        content.get(bytes);
        return bytes;
    }

    private void setRecord(int index, ByteBuffer record) {
        content.position(index * recordLen);
        content.put(record);
    }

    private void setRecord(int index, byte[] record) {
        content.position(index * recordLen);
        content.put(record);
    }

    private void heapAdjust(int parent) {
        int originParent = parent;
        long temp = getRecordLongKey(parent);
        byte[] tempBytes = getRecordBytes(parent);

        int child = 2 * parent + 1; // 先获得左孩子

        while (child < capacity) {
            long childKey = getRecordLongKey(child);
            long rightKey;
            if (child + 1 < capacity &&
                    comparator.compare(childKey,
                            (rightKey = getRecordLongKey(child + 1))) > 0) {
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
        content.position(0);
        content.put(root);

        heapAdjust(0);
    }

    /**
     * 得到堆的根节点
     *
     * @return
     */
    public ByteBuffer getRoot() {
        content.position(0);
        content.limit(recordLen);
        return content.slice();
    }

    private void init() {
        if ( capacity == 0 ){
            return;
        }
        for (int i = (capacity - 1) / 2; i >= 0; i--) {
            heapAdjust(i);
        }
    }

    /**
     * 按序消费堆中元素
     *
     * @param consumer
     * @return 剩余的无法在此轮进行排序的元素
     */
    public ByteBuffer forEach(Consumer<ByteBuffer> consumer) {

        content.limit(originCapacity * recordLen);
        content.position(capacity * recordLen);
        ByteBuffer rest = content.slice();

        if ( capacity != 0 ){
            consumer.accept(getRoot());
        }

        while (shrink(null) != 0){
            consumer.accept(getRoot());
        }

        return rest;
    }

    private void swap(int index0, int index1) {
        byte[] temp = getRecordBytes(index0);
        setRecord(index0, getRecordBuffer(index1));
        setRecord(index1, temp);
    }

    public int shrink(ByteBuffer newRecord) {
        if( capacity == 0 ){
            return 0;
        }

        capacity -= 1;

        swap(0, capacity);
        heapAdjust(0);

        if (newRecord != null) {
            content.clear();
            content.position(capacity * recordLen);
            content.put(newRecord);
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
        sb.append("]");

        return sb.toString();
    }
}
