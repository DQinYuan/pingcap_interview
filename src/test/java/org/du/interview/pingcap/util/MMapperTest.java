package org.du.interview.pingcap.util;

import org.junit.Test;

public class MMapperTest {

    @Test
    public void test() throws Exception {

        MMapper mMapper = new MMapper("temp/G10.dat", 9 * 1024 * 1024 * 1024);
        System.out.println(mMapper.getByte(0));
        System.out.println(mMapper.getByte(17));
        System.out.println(mMapper.getLong(2147483647));
        System.out.println(Integer.MAX_VALUE);
    }

}
