package org.du.interview.pingcap.util;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class MmapTest {

    @Ignore
    @Test
    public void test(){
        int G = 1024 * 1024 * 1024;

        Path path = Paths.get("temp", "G10.dat");
        Mmap mmap = new Mmap(path, 0L, G, FileChannel.MapMode.READ_WRITE);

        System.out.println(mmap.getByte(0));
        System.out.println(mmap.getByte(3111));
        System.out.println(mmap.getByte(3768));

        System.out.println(Arrays.toString(mmap.getBytes(0, 10)));

        mmap.unmap(true);

        Mmap mmap1 = new Mmap(path, 4096, G, FileChannel.MapMode.READ_WRITE);

        System.out.println(mmap1.getByte(0));
        System.out.println(mmap1.getByte(10));

        mmap1.unmap(true);

        Mmap mmap2 = new Mmap(path, 2147483647L, G, FileChannel.MapMode.READ_WRITE);

        //一轮1G循环的末尾
        System.out.println(mmap2.getByte(0)); //23
        //重头开始  0
        System.out.println(mmap2.getByte(1));
        //198
        System.out.println(mmap2.getByte(199));

        mmap2.unmap(true);
    }

}
