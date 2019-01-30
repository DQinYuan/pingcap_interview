package org.du.interview.pingcap.util;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassPathUtil {

    public static Path relative(Class klass , String path, String... paths){
        Path classPath = null;
        try {
            classPath = Paths.get(klass
                    .getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return classPath.resolve(Paths.get(path, paths));
    }

}