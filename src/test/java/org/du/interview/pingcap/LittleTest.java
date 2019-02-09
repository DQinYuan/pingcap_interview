package org.du.interview.pingcap;

import org.du.interview.pingcap.util.Constant;
import org.du.interview.pingcap.util.ReflectiveUtil;
import org.junit.Ignore;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.FileChannelImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(3);
        System.out.println(byteBuffer.remaining());
    }

    @Ignore
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

    @Ignore
    @Test
    public void mmaptest() throws IOException {
        Path path = Paths.get("temp", "G10.dat");
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
    }

    @Test
    public void initDsTest() throws Exception {
        Method initIDs = ReflectiveUtil.getMethod(FileChannelImpl.class, "initIDs");
        long allocationGranularity = (long) initIDs.invoke(null);
        System.out.println(allocationGranularity);
    }

    @Ignore
    @Test
    public void intOverflowTest(){
        System.out.println(Integer.MAX_VALUE);
        //int + long 会被强转为long
        System.out.println(2147483647 + 1L);
    }

    @Ignore
    @Test
    public void fileDispatcher() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        //,写入位置,写入大小,文件位置
        //FileDescriptor var1, long var2, int var4, long var5
        Method pread = ReflectiveUtil.getMethod(Class.forName("sun.nio.ch.FileDispatcherImpl"),
                "pread", Class.forName("java.io.FileDescriptor"), long.class,
                int.class, long.class);


        Field fd = ReflectiveUtil.getField(FileChannelImpl.class, "fd");
    }

    @Ignore
    @Test
    public void directBuffer() throws NoSuchMethodException, ClassNotFoundException {
        Constructor<?> constructor = ReflectiveUtil.getConstructor(Class.forName("java.nio.DirectByteBuffer"),
                long.class, int.class);
    }

    @Ignore
    @Test
    public void randomAccessFileTest() throws IOException {
        RandomAccessFile raf= new RandomAccessFile("data/test.dat", "rw");
        raf.setLength(1024);

        FileChannel channel = raf.getChannel();
        System.out.println(channel.size());
    }

    @Test
    public void maxInt(){
        System.out.println(Integer.MAX_VALUE);
    }

    @Ignore
    @Test
    public void iopsTest() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) Constant.G);

        int gNum = 134217728;

        for (int i = 0; i < gNum; i++){
            byteBuffer.putLong(i);
        }

        byteBuffer.flip();
        System.out.println("bybuffer filled:" + byteBuffer.limit());

        FileChannel channel = FileChannel.open(Paths.get("temp", "iopstest.dat"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        long start = System.currentTimeMillis();
        channel.write(byteBuffer);
        System.out.println("1G write, time used:" + (System.currentTimeMillis() - start) + " ms");
    }

    @Ignore
    @Test
    public void channelTest() throws IOException {
        /**
         * FileChannel.open的特点是不会覆盖原文件,原文件没有被修改的地方该什么样还是什么样
         *
         * 加了StandardOpenOption.TRUNCATE_EXISTING即可实现文件覆盖
         */
        FileChannel channel = FileChannel.open(Paths.get("temp", "channeltest.dat"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        byteBuffer.put((byte) 'p');
        byteBuffer.put((byte) 'b');
        byteBuffer.put((byte) 'c');
        byteBuffer.put((byte) 'd');
        byteBuffer.flip();

        channel.write(byteBuffer);

        channel.close();
    }

    /**
     * 使用ramdomAccessFile的write方法抑或从其中获得的channel也都不能覆盖文件
     * @throws IOException
     */
    @Ignore
    @Test
    public void randomAccessFileChannelTest() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("temp/channeltest.dat", "rw");
        randomAccessFile.setLength(0);
        randomAccessFile.setLength(8);
        randomAccessFile.write(new byte[]{'m'});
    }

}