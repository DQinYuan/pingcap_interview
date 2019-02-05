package org.du.interview.pingcap.util;

import org.junit.Ignore;
import org.junit.Test;

public class MMapperTest {

    /**
     * 经过测试,这个MMapper用不了,get一个long的偏移时会发生crash
     * 还是拆分是正道
     * @throws Exception
     */
    @Ignore
    @Test
    public void test() throws Exception {

        MMapper mMapper = new MMapper("temp/G10.dat", 4 * 1024);
        System.out.println(mMapper.getByte(0));
        System.out.println(mMapper.getByte(17));
    }

}
