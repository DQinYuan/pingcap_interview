package org.du.interview.pingcap.sort;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

public class ESortTest {

    @Ignore
    @Test
    public void initRunTest(){
        ESort.initRun(Paths.get("temp", "user.dat"),
                Paths.get("temp"), 16, (l1, l2) -> Long.compare(l1, l2));
    }

    @Ignore
    @Test
    public void multiWayMergeTest(){
        ESort.multiwayMerge(Paths.get("temp"),
                Paths.get("data", "result.dat"), 16);
    }

    @Ignore
    @Test
    public void sortTest(){
        ESort.sort(Paths.get("data", "origin.dat"),
                Paths.get("data", "result.dat"),
                Paths.get("temp"),
                16, (l1, l2) -> Long.compare(l1, l2));
    }

    @Ignore
    @Test
    public void userSortTest(){
        ESort.sort(Paths.get("data", "user.dat"),
                Paths.get("temp", "user_tmp.dat"),
                Paths.get("temp"),
                16, (l1, l2) -> Long.compare(l1, l2));
    }

}
