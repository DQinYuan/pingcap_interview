package org.du.interview.pingcap.sort;

import org.du.interview.pingcap.Config;
import org.du.interview.pingcap.util.BigByteBuffer;
import org.du.interview.pingcap.util.FastDirectByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class ReadBuffer {

    private FileChannel channel;
    private ByteBuffer buffer;

    private int bufferOffset;

    private int bufferSize;

    private Path filePath;

    public ReadBuffer(Path in){
        try {
            this.filePath = in;
            channel = FileChannel.open(in);
            buffer = ByteBuffer.allocateDirect(Config.READ_BUFFER_SIZE);
            this.bufferOffset = 0;
            this.bufferSize = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 这里的实现作了一个妥协: fileChannel必须至少能填满该buffer
     * @param contentBuffer
     */
    public void read(BigByteBuffer contentBuffer){
        contentBuffer.fillFromFileChannel(channel);
    }


    /**
     *
     * @param recordLen
     * @return 当没有下一条记录时,返回null
     */
    public ByteBuffer nextRecord(int recordLen){
        if (!ensureCapacity(recordLen)){
            return null;
        }
        buffer.position(bufferOffset);
        bufferOffset += recordLen;
        buffer.limit(bufferOffset);
        return buffer.slice();
    }

    /**
     *
     * @param recordLen
     * @return 文件是否还有剩余空间
     */
    private boolean ensureCapacity(int recordLen){
        if ( bufferRemaining() < recordLen ){
            try {
                if ( fileRemaining() < recordLen ){
                    return false;
                }
                buffer.clear();
                channel.read(buffer);
                buffer.flip();
                bufferSize = buffer.limit();
                bufferOffset = 0;
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private int bufferRemaining(){
        return bufferSize - bufferOffset;
    }

    public long fileRemaining(){
        try {
            return channel.size() - channel.position();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
