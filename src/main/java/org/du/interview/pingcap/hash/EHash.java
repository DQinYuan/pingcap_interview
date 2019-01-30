package org.du.interview.pingcap.hash;

import org.du.interview.pingcap.sort.ReadBuffer;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class EHash {


    public static HashFile hash(Path in, Path out, int recordLen){
        ReadBuffer readBuffer = new ReadBuffer(in);

        HashFile hashFile = new HashFile(out,
                (int) readBuffer.fileRemaining(), recordLen);

        ByteBuffer record;
        while ( (record = readBuffer.nextRecord(recordLen)) != null ){
            hashFile.put(record.getLong(), record.getLong());
        }

        return hashFile;
    }

}
