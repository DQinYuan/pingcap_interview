package org.du.interview.pingcap.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MockData {

    private static class Trade{
        long userId;
        long itemId;

        public Trade(long userId, long itemId) {
            this.userId = userId;
            this.itemId = itemId;
        }
    }

    private static class Item{
        long itemId;
        long price;

        public Item(long itemId, long price) {
            this.itemId = itemId;
            this.price = price;
        }
    }

    @Test
    public void file10G() throws IOException {
        int G = 1024 * 1024 * 1024;
        int GNum = 2;
        Path path10G = Paths.get("temp", "G2.dat");
        FileChannel channel =
                FileChannel.open(path10G, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] bytes = new byte[G];

        for ( int i = 0; i < bytes.length; i++ ){
            bytes[i] = (byte) (i % 100);
        }

        for ( int i = 0; i < GNum; i++ ){
            channel.write(ByteBuffer.wrap(bytes));
        }

        channel.close();
    }

    @Ignore
    @Test
    public void userItemAndPrice() throws IOException {
        int num = 256;
        int dup = 4;
        int detailNum = num * dup;
        Trade[] trades = new Trade[detailNum];

        //userId取值范围为0~num-1    itemId取值范围为0~num+2
        for ( int i = 0; i < num; i++ ){
            for ( int j = 0; j < dup; j++ ){
                trades[i * dup + j] = new Trade(i, i + j);
            }
        }

        ArrayUtils.shuffle(trades);

        FileChannel userChannel = FileChannel.open(Paths.get("data", "user.dat"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        ByteBuffer oneRecord = ByteBuffer.allocate(16);
        for (Trade trade : trades) {
            oneRecord.putLong(trade.userId);
            oneRecord.putLong(trade.itemId);
            oneRecord.flip();
            userChannel.write(oneRecord);
            oneRecord.clear();
        }

        FileChannel itemChannel = FileChannel.open(Paths.get("data", "item.dat"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Item[] items = new Item[num + 3];
        for ( int i = 0; i < items.length; i++ ){
            items[i] = new Item(i, i - 1);
        }

        ArrayUtils.shuffle(items);

        for (Item item : items) {
            oneRecord.putLong(item.itemId);
            oneRecord.putLong(item.price);
            oneRecord.flip();
            itemChannel.write(oneRecord);
            oneRecord.clear();
        }

        userChannel.close();
        itemChannel.close();

    }

    @Ignore
    @Test
    public void mockData() throws IOException {
        int num = 256 * 4;
        long[] testData = new long[num];

        for ( int i = 0; i < num; i++ ){
            testData[i] = i;
        }

        Path data = Paths.get("data", "origin.dat");

        FileChannel channel = FileChannel.open(data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        ByteBuffer oneRecord = ByteBuffer.allocate(16);

        ArrayUtils.shuffle(testData);

        for (long testKey : testData) {
            oneRecord.putLong(testKey);
            oneRecord.putLong(testKey - 3);
            oneRecord.flip();
            channel.write(oneRecord);
            oneRecord.clear();
        }

        channel.close();
    }

    @Test
    public void showContent() throws IOException {
        decodeFile(Paths.get("temp", "tmp-0.dat"));
        //decodeFile(Paths.get("data", "origin.dat"));
    }


    private void decodeFile(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

        while (channel.read(byteBuffer) != -1){
            byteBuffer.flip();
            long key = byteBuffer.getLong();
            long value = byteBuffer.getLong();
            System.out.println(String.format("%4d %4d", key, value));
            byteBuffer.clear();
        }
    }

}
