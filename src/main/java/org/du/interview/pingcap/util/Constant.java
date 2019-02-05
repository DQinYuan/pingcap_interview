package org.du.interview.pingcap.util;

import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;

public class Constant {

    /**
     * 操作系统每个内存页的大小
     */
    public static final long allocationGranularity;
    public static final long G = 1024 * 1024 * 1024;

    static {
        Method initIDs = null;
        try {
            initIDs = ReflectiveUtil.getMethod(FileChannelImpl.class, "initIDs");
            allocationGranularity = (long) initIDs.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
