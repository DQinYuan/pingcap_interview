package org.du.interview.pingcap.hash;

import org.junit.Test;

import java.nio.file.Paths;

public class EHashTest {

    @Test
    public void hashTest(){
        HashFile hashFile = EHash.hash(Paths.get("data", "item.dat"),
                Paths.get("data", "hash.dat"), 16);

        System.out.println(hashFile.get(200));
        System.out.println(hashFile.get(0));
        System.out.println(hashFile.get(155));
    }


}
