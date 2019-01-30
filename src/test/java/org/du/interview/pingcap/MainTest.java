package org.du.interview.pingcap;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

public class MainTest {

    @Ignore
    @Test
    public void groupByAndSumTest(){
        Main.groupByAndSum(Paths.get("data", "user.dat"),
                Paths.get("data", "item.dat"),
                Paths.get("data", "out.dat"),
                Paths.get("temp"), 16);
    }

}
