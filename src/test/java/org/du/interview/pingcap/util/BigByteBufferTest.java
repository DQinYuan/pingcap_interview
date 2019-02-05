package org.du.interview.pingcap.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class BigByteBufferTest {

    private static final long G10 = 10L * 1024L * 1024L * 1024L;

    @Test
    public void test(){
        BigByteBuffer bigByteBuffer = new BigByteBuffer(G10);
        bigByteBuffer.setLong(2147483649L, 1098);
        System.out.println(bigByteBuffer.getLong(2147483649L));

        bigByteBuffer.setBytes(2147483649L, new byte[]{10, 99});
        System.out.println(bigByteBuffer.getByte(2147483649L));
        System.out.println(bigByteBuffer.getByte(2147483650L));

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(3);
        byteBuffer.put((byte) 93);
        byteBuffer.put((byte) 33);
        byteBuffer.flip();
        bigByteBuffer.setByteBuffer(2147483658L, byteBuffer);
        System.out.println(bigByteBuffer.getByte(2147483658L));
        System.out.println(bigByteBuffer.getByte(2147483659L));
    }

    @Ignore
    @Test
    public void fillFromFileChannelTest() throws IOException {
        BigByteBuffer bigByteBuffer = new BigByteBuffer(G10);

        Path path = Paths.get("temp", "G10.dat");
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);

        long start = System.currentTimeMillis();
        bigByteBuffer.fillFromFileChannel(channel);
        System.out.println("time used:" + (System.currentTimeMillis() - start) + " ms");

        System.out.println(bigByteBuffer.getByte(0));//0
        System.out.println(bigByteBuffer.getByte(3111));//11
        System.out.println(bigByteBuffer.getByte(3768));//68

        System.out.println(bigByteBuffer.getByte(4096));//96
        System.out.println(bigByteBuffer.getByte(5006));//6

        //23
        System.out.println(bigByteBuffer.getByte(2147483647L));
        //0
        System.out.println(bigByteBuffer.getByte(2147483648L));
        //98
        System.out.println(bigByteBuffer.getByte(2147483846L));

        //22
        System.out.println(bigByteBuffer.getByte(10737418238L));
        //23
        System.out.println(bigByteBuffer.getByte(10737418239L));

        //位于同block
        //[40, 41, 42, 43, 44, 45, 46, 47, 48, 49]
        System.out.println(Arrays.toString(bigByteBuffer.getBytes(40, 10)));

        //位于不同block
        //[20, 21, 22, 23, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        System.out.println(Arrays.toString(bigByteBuffer.getBytes(Constant.G - 4L, 15)));

        //跨block setBytes
        bigByteBuffer.setBytes(Constant.G - 4L, new byte[]{20, 29, 22, 23, 0, 1, 2, 3, 4, 5, 6, 7, 8, 99, 10});
        System.out.println(Arrays.toString(bigByteBuffer.getBytes(Constant.G - 4L, 15)));

        ByteBuffer byteBuffer = bigByteBuffer.wrapInByteBuffer(10737418238L, 2);
        //22
        System.out.println(byteBuffer.get());
        //23
        System.out.println(byteBuffer.get());

        BigByteBuffer slice = bigByteBuffer.slice(2147483640L, 2147483640L);
        //16
        System.out.println(slice.getByte(0));
        //23
        System.out.println(slice.getByte(7));
        //19
        System.out.println(slice.getByte(1073741827L));
    }



}
