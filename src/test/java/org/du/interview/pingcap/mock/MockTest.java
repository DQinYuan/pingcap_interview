package org.du.interview.pingcap.mock;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

public class MockTest {

    @Ignore
    @Test
    public void littleDataMockTest(){
        Mock.mock(Paths.get("data"), 2056);
    }

    /**
     * 构造5G数据所需时间
     * 16k落盘  56554ms
     * 64k落盘  62367ms
     *  4k落盘  58608ms
     */
    @Ignore
    @Test
    public void mockBigData(){
        Mock.mock(Paths.get("temp"), 16777216);
    }

}
