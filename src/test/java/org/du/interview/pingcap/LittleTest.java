package org.du.interview.pingcap;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class LittleTest {

    @Test
    public void test(){
        ByteBuffer a = ByteBuffer.wrap(new byte[]{10, 11, 20, 39});
        ByteBuffer b = ByteBuffer.allocate(2);

        a.position(1);
        //3不包含
        a.limit(3);

        b.put(a.slice());

        System.out.println(Arrays.toString(b.array()));
    }

    @Test
    public void test2(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        System.out.println(byteBuffer.remaining());
    }

    @Test
    public void pathTest(){
        Path path = Paths.get("data/item.dat");
        System.out.println(path.toAbsolutePath());
        System.out.println(Files.exists(path));
    }

    @Test
    public void propertyTest(){
        String path = System.getProperty("path");
        System.out.println(path);
    }

}