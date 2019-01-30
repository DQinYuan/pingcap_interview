package org.du.interview.pingcap.sort;

import org.du.interview.pingcap.Config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WriteBuffer {

    private FileChannel fileChannel;

    private ByteBuffer buffer;

    private Path filePath;

    public WriteBuffer(Path out) {
        buffer = ByteBuffer.allocateDirect(Config.WRITE_BUFFER_SIZE);
        this.filePath = out;
    }

    private void channelWrite(ByteBuffer block) {
        if (block.limit() > 0) {
            try {
                if (fileChannel == null) {
                    fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }

                fileChannel.write(block);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(ByteBuffer record) {
        ensureCapacity(record.limit());
        buffer.put(record);
    }

    public void write(long key, long value){
        ensureCapacity(16);
        buffer.putLong(key);
        buffer.putLong(value);
    }

    public void flushAndClose() {
        try {
            buffer.flip();
            channelWrite(buffer);
            if (fileChannel != null) {
                fileChannel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureCapacity(int recordLen) {
        if (buffer.remaining() < recordLen) {
            buffer.flip();
            channelWrite(buffer);
            buffer.clear();
        }
    }

}
