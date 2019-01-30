package org.du.interview.pingcap;

import org.junit.Test;

import java.util.Arrays;

public class HeapSortTest {


    private void heapAdjust(int[] array, int parent, int length) {

        int temp = array[parent]; // temp保存父节点

        int child = 2 * parent + 1; // 先获得左孩子


        while (child < length) {
            // 如果有右孩子结点，并且右孩子结点的值小于左孩子结点，则选取右孩子结点
            if (child + 1 < length && array[child] > array[child + 1]) {
                child++;
            }

            // 如果父结点的值已经小于孩子结点的值，则直接结束
            if (temp <= array[child])
                break;


            // 把孩子结点的值赋给父结点
            array[parent] = array[child];

            // 选取孩子结点的左孩子结点,继续向下筛选
            parent = child;

            child = 2 * child + 1;
        }

        array[parent] = temp;
    }

    private void init(int[] list) {
        // 循环建立初始堆
        for (int i = (list.length - 1) / 2; i >= 0; i--) {
            heapAdjust(list, i,
                    list.length);
        }
    }

    /**
     * [10, 20, 40, 30, 70, 90, 80, 60, 50]
     */
    @Test
    public void test() {
        int[] list = new int[]{50, 10, 90, 30, 70, 40, 80, 60, 20};

        init(list);

        System.out.println(Arrays.toString(list));
    }


}
