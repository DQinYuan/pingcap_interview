package org.du.interview.pingcap.sort;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface LongKeyGetter {

    long get(ByteBuffer byteBuffer);

}
