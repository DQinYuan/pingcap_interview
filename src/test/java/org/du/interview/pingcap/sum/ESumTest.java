package org.du.interview.pingcap.sum;

import org.du.interview.pingcap.hash.EHash;
import org.du.interview.pingcap.hash.HashFile;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

public class ESumTest {

    @Ignore
    @Test
    public void sumTest(){
        HashFile hashFile = EHash.hash(Paths.get("data", "item.dat"),
                Paths.get("data", "hash.dat"), 16);
        ESum.sum(Paths.get("temp", "user_tmp.dat"), hashFile,
                Paths.get("data", "test_res.dat"), 16);
    }

}
