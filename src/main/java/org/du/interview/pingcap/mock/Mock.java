package org.du.interview.pingcap.mock;

import org.du.interview.pingcap.util.ArrayUtils;
import org.du.interview.pingcap.util.ByteBufferSupport;
import org.du.interview.pingcap.util.EPathUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Mock {

    private static class Trade {
        long userId;
        long itemId;

        public Trade() {
        }

        public Trade(long userId, long itemId) {
            this.userId = userId;
            this.itemId = itemId;
        }
    }

    private static class Item {
        long itemId;
        long price;

        public Item(long itemId, long price) {
            this.itemId = itemId;
            this.price = price;
        }
    }

    /**
     * user表记录数: num * 4
     * item表记录数: num * 4  但是真正有用的数据也就是num+3个
     *
     * @param mockPath 测试数据摆放的路径
     * @param num      测试数据生成的用户数目
     */
    public static void mock(Path mockPath, int num) {

        EPathUtil.createIfNotExist(mockPath);

        long start = System.currentTimeMillis();
        int dup = 4;
        int detailNum = num * dup;

        //每次落盘的记录大小 16k, 数量为1024条记录
        int diskUnit = 1024;
        ByteBuffer bigBuffer = ByteBuffer.allocateDirect(diskUnit * 16);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(diskUnit * 16);
        //切割成bytebuffer数组
        ByteBuffer[] byteBuffers = ByteBufferSupport.split(bigBuffer, 16);
        System.out.println("bytebuffer array length:" + byteBuffers.length);

        //userId取值范围为0~num-1    itemId取值范围为0~num+2
        try(FileChannel userChannel = FileChannel.open(mockPath.resolve("user.dat"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            FileChannel itemChannel = FileChannel.open(mockPath.resolve("item.dat"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            //user表落盘
            for (int i = 0; i < num; i++) {
                for (int j = 0; j < dup; j++) {
                    writeInByteBuffer(i * dup + j, userChannel, byteBuffers, i, i + j, tempBuffer);
                }
            }

            writeAll(userChannel, byteBuffers, tempBuffer);


            for (int i = 0; i < detailNum; i++) {
                writeInByteBuffer(i, itemChannel, byteBuffers, i, i - 1, tempBuffer);
            }

            writeAll(itemChannel, byteBuffers, tempBuffer);
        } catch (IOException e) {
            throw new RuntimeException("mock数据伪造失败");
        }


        System.out.println("mockdata over, user num:" + detailNum + ", item num:" + (num + 3)
            + ", time used: " + (System.currentTimeMillis() - start));
    }

    private static void writeInByteBuffer(int pos, FileChannel channel, ByteBuffer[] byteBuffers,
                                          long key, long value, ByteBuffer tempBuffer) throws IOException {
        int index = pos % byteBuffers.length;
        if (pos != 0 && index == 0){
            writeAll(channel, byteBuffers, tempBuffer);
        }
        byteBuffers[index].putLong(key);
        byteBuffers[index].putLong(value);
    }

    private static void writeAll(FileChannel channel, ByteBuffer[] byteBuffers
        , ByteBuffer tempBuffer) throws IOException {
        ArrayUtils.shuffle(byteBuffers);
        tempBuffer.clear();
        for (ByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.flip();
            tempBuffer.put(byteBuffer);
            byteBuffer.clear();
        }
        tempBuffer.flip();
        channel.write(tempBuffer);
    }

}
