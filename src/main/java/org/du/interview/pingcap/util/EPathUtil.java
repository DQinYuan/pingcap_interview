package org.du.interview.pingcap.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class EPathUtil {

    /**
     * data文件名格式为 data-threadId
     * @return
     */
    public static Path createTmpPath(Number fileNum, Path path){
        return path.resolve(String.format("tmp-%d.dat", fileNum));
    }

    public static Stream<Path> list(Path path){
        try {
            return Files.list(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(path.toAbsolutePath() + "目录不存在");
        }
    }

    public static Stream<Path> list(Path path, String prefix){
        return list(path).filter(sub -> sub.getFileName().toString().startsWith(prefix));
    }

    public static Stream<Path> listTmpFile(Path path){
        return list(path, "tmp");
    }

    public static void createIfNotExist(Path path){
        if ( !Files.exists(path)){
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("mock文件夹创建失败");
            }
        }
    }

}
