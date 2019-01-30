package org.du.interview.pingcap.hash;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

public class HashFileTest {

    @Ignore
    @Test
    public void test(){
        HashFile hashFile = new HashFile(Paths.get("data", "hash.dat"), 12, 16);
        hashFile.put(1, 100);
        hashFile.put(17, 190);
        hashFile.put(99, 88);

        System.out.println(hashFile.get(1));
        System.out.println(hashFile.get(17));
        System.out.println(hashFile.get(99));
    }

}
