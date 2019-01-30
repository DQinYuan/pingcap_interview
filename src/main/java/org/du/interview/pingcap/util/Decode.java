package org.du.interview.pingcap.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class Decode {

    public static void show(Path path) {
        try {
            decodeFile(path);
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败");
        }
    }


    private static void decodeFile(Path path) throws IOException {
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
