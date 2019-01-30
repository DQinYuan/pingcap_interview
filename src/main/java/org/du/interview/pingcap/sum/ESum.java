package org.du.interview.pingcap.sum;

import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.LongLongMap;
import org.du.interview.pingcap.hash.HashFile;
import org.du.interview.pingcap.sort.ReadBuffer;
import org.du.interview.pingcap.sort.WriteBuffer;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ESum {

    public static void sum(Path orderedFile, HashFile hashFile, Path out, int recordLen){
        ReadBuffer readBuffer = new ReadBuffer(orderedFile);
        WriteBuffer writeBuffer = new WriteBuffer(out);

        LongLongMap userSum = new LongLongHashMap();

        long lastUserId;
        ByteBuffer oneRecord;

        oneRecord = readBuffer.nextRecord(recordLen);
        lastUserId = oneRecord.getLong();
        long price = hashFile.get(oneRecord.getLong());

        userSum.putOrAdd(lastUserId, price, price);

        long userId = 0;
        while ((oneRecord = readBuffer.nextRecord(recordLen)) != null){
            userId = oneRecord.getLong();
            price = hashFile.get(oneRecord.getLong());
            userSum.putOrAdd(userId, price, price);

            //发现不等则说明上一个userId已经全部加完了,可以进行落盘
            if ( userId != lastUserId ){
                writeBuffer.write(lastUserId, userSum.get(lastUserId));
                userSum.remove(lastUserId);
            }

            lastUserId = userId;
        }

        writeBuffer.write(userId, userSum.get(userId));
        userSum.remove(userId);

        writeBuffer.flushAndClose();
    }

}
