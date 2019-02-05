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

public class FastDirectBufferTest {

    @Test
    public void test(){
        FastDirectByteBuffer directByteBuffer = new FastDirectByteBuffer(1024);
        directByteBuffer.setLong(10, 105L);

        System.out.println(directByteBuffer.getLong(10));//105

        directByteBuffer.setBytes(new byte[]{13, 89, 99}, 10);
        System.out.println(directByteBuffer.getByte(10));
        System.out.println(directByteBuffer.getByte(11));//89
        System.out.println(directByteBuffer.getByte(12));

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10);
        byteBuffer.put((byte) 99);
        byteBuffer.put((byte) 85);
        byteBuffer.flip();
        directByteBuffer.setByteBuffer(9, byteBuffer);
        System.out.println(directByteBuffer.getByte(9));
        System.out.println(directByteBuffer.getByte(10));
        System.out.println(directByteBuffer.getByte(11));

        FastDirectByteBuffer slice = directByteBuffer.slice(9, 3);
        System.out.println(slice.getByte(0));
        System.out.println(slice.getByte(1));

        directByteBuffer.free();
    }

    @Ignore
    @Test
    public void fillFromFileChannelTest() throws IOException {
        FastDirectByteBuffer directByteBuffer = new FastDirectByteBuffer((int) Constant.G);
        Path path = Paths.get("temp", "G10.dat");
        FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        directByteBuffer.fillFromFileChannel(channel, 0);
        System.out.println(directByteBuffer.getByte(1));
        System.out.println(Arrays.toString(directByteBuffer.getBytes(1, 10)));
        System.out.println(directByteBuffer.getByte(198));
        System.out.println(directByteBuffer.getByte(1073741820));

        byte[] dst = new byte[20];
        directByteBuffer.getBytes(dst, 5, 3, 10);
        System.out.println(Arrays.toString(dst));

        ByteBuffer byteBuffer = directByteBuffer.wrapInByteBuffer();
        System.out.println(byteBuffer.get(198));
        System.out.println(byteBuffer.get(1073741820));

        ByteBuffer byteBuffer1 = directByteBuffer.wrapInByteBuffer(198, 10);
        System.out.println(byteBuffer1.get());
        System.out.println(byteBuffer1.get());
        System.out.println(byteBuffer1.get());

        directByteBuffer.free();
    }

    @Ignore
    @Test
    public void fillFromFileChannelTest2() throws IOException {
        FastDirectByteBuffer directByteBuffer = new FastDirectByteBuffer(6);
        Path path = Paths.get("temp", "G10.dat");
        FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        directByteBuffer.fillFromFileChannel(channel);
        System.out.println(directByteBuffer.getByte(0));

        directByteBuffer.fillFromFileChannel(channel);
        System.out.println(directByteBuffer.getByte(0));
    }

    @Test
    public void testLong(){
        FastDirectByteBuffer directByteBuffer = new FastDirectByteBuffer(8);
        directByteBuffer.setLong(0, 89);

        System.out.println(directByteBuffer.getLong(0));

        ByteBuffer byteBuffer = directByteBuffer.wrapInByteBuffer(0, 8);
        System.out.println(byteBuffer.getLong());
    }

}
