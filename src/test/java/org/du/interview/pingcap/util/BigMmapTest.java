package org.du.interview.pingcap.util;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class BigMmapTest {

    @Ignore
    @Test
    public void test(){
        BigMmap bigMmap = new BigMmap(Paths.get("temp", "G10.dat"),
                FileChannel.MapMode.READ_WRITE);

        System.out.println(bigMmap.getByte(0));
        System.out.println(bigMmap.getByte(3111));
        System.out.println(bigMmap.getByte(3768));

        System.out.println(bigMmap.getByte(4096));
        System.out.println(bigMmap.getByte(5006));

        //23
        System.out.println(bigMmap.getByte(2147483647L));
        //0
        System.out.println(bigMmap.getByte(2147483648L));
        //98
        System.out.println(bigMmap.getByte(2147483846L));

        //22
        System.out.println(bigMmap.getByte(10737418238L));
        //23
        System.out.println(bigMmap.getByte(10737418239L));

        bigMmap.unmap();
    }

    @Ignore
    @Test
    public void crossBlockTest(){
        BigMmap bigMmap = new BigMmap(Paths.get("temp", "G2.dat"), FileChannel.MapMode.READ_WRITE);

        //bigMmap.setLong(Constant.G - 2, 10737418239L);

        System.out.println(bigMmap.getLong(Constant.G - 2));

    }

}
