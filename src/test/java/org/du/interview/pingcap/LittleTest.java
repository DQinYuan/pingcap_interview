package org.du.interview.pingcap;

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


}