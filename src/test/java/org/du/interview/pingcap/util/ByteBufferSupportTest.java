package org.du.interview.pingcap.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class ByteBufferSupportTest {

    @Test
    public void splitTest(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16 * 11);
        ByteBuffer[] split = ByteBufferSupport.split(byteBuffer, 16);
        for (ByteBuffer buffer : split) {
            Assert.assertEquals(buffer.capacity(), 16);
        }
    }

}
