package org.du.interview.pingcap.util.fastbuffer;

import org.du.interview.pingcap.util.MyUnsafe;

import java.nio.ByteBuffer;


/**
 * An implementation of the ByteBufferReader using sun.misc.Unsafe. This provides very high
 * throughput read of various primitive types from a HeapByteBuffer, but can potentially
 * crash the JVM if the implementation is faulty.
 */
public class UnsafeHeapByteBufferReader implements ByteBufferReader {

  private long offset;
  private byte[] array;

  public UnsafeHeapByteBufferReader(ByteBuffer buf) {
    if (!buf.hasArray()) {
      throw new UnsupportedOperationException("buf (" + buf + ") must have a backing array");
    }
    offset = MyUnsafe.BYTE_ARRAY_BASE_OFFSET;
    array = buf.array();
  }

  public byte getByte() {
    byte v = MyUnsafe.UNSAFE.getByte(array, offset);
    offset += 1;
    return v;
  }

  public byte[] getBytes(byte[] dst, int len) {
    MyUnsafe.UNSAFE.copyMemory(array, offset, dst, MyUnsafe.BYTE_ARRAY_BASE_OFFSET, len);
    return dst;
  }

  public short getShort() {
    short v = MyUnsafe.UNSAFE.getShort(array, offset);
    offset += 2;
    return v;
  }

  public int getInt() {
    int v = MyUnsafe.UNSAFE.getInt(array, offset);
    offset += 4;
    return v;
  }

  public long getLong() {
    long v = MyUnsafe.UNSAFE.getLong(array, offset);
    offset += 8;
    return v;
  }

  public float getFloat() {
    float v = MyUnsafe.UNSAFE.getFloat(array, offset);
    offset += 4;
    return v;
  }

  public double getDouble() {
    double v = MyUnsafe.UNSAFE.getDouble(array, offset);
    offset += 8;
    return v;
  }

  public int position() {
    return (int) (offset - MyUnsafe.BYTE_ARRAY_BASE_OFFSET);
  }

  public ByteBufferReader position(int newPosition) {
    offset = MyUnsafe.BYTE_ARRAY_BASE_OFFSET + newPosition;
    return this;
  }
}
