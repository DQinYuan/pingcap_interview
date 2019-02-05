package org.du.interview.pingcap.mock;

import org.du.interview.pingcap.util.ArrayUtils;
import org.du.interview.pingcap.util.EPathUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Mock {

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

    /**
     * user表记录数: num * 4
     * item表记录数: num + 3
     * @param mockPath 测试数据摆放的路径
     * @param num 测试数据生成的用户数目
     */
    public static void mock(Path mockPath, int num){

        EPathUtil.createIfNotExist(mockPath);

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

        FileChannel userChannel = null;
        try {
            userChannel = FileChannel.open(mockPath.resolve("user.dat"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            ByteBuffer oneRecord = ByteBuffer.allocate(16);
            for (Trade trade : trades) {
                oneRecord.putLong(trade.userId);
                oneRecord.putLong(trade.itemId);
                oneRecord.flip();
                userChannel.write(oneRecord);
                oneRecord.clear();
            }

            FileChannel itemChannel = FileChannel.open(mockPath.resolve("item.dat"),
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
        } catch (IOException e) {
            throw new RuntimeException("mock数据伪造失败");
        }



    }

}
