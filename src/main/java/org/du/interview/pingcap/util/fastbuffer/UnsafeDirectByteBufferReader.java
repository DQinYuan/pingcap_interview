package org.du.interview.pingcap.util.fastbuffer;



import org.du.interview.pingcap.util.MyUnsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;


/**
 * An implementation of the ByteBufferReader using sun.misc.Unsafe. This provides very high
 * throughput read of various primitive types from a ByteBuffer, but can potentially
 * crash the JVM if the implementation is faulty.
 */
public class UnsafeDirectByteBufferReader implements ByteBufferReader {

  private long baseOffset;
  private long offset;

  public UnsafeDirectByteBufferReader(ByteBuffer buf) {
    baseOffset = getMemoryAddress(buf);
    offset = baseOffset;
  }

  public byte getByte() {
    byte v = MyUnsafe.UNSAFE.getByte(offset);
    offset += 1;
    return v;
  }

  public byte[] getBytes(byte[] dst, int len) {
    MyUnsafe.UNSAFE.copyMemory(null, offset, dst, MyUnsafe.BYTE_ARRAY_BASE_OFFSET, len);
    return dst;
  }

  public short getShort() {
    short v = MyUnsafe.UNSAFE.getShort(offset);
    offset += 2;
    return v;
  }

  public int getInt() {
    int v = MyUnsafe.UNSAFE.getInt(offset);
    offset += 4;
    return v;
  }

  public long getLong() {
    long v = MyUnsafe.UNSAFE.getLong(offset);
    offset += 8;
    return v;
  }

  public float getFloat() {
    float v = MyUnsafe.UNSAFE.getFloat(offset);
    offset += 4;
    return v;
  }

  public double getDouble() {
    double v = MyUnsafe.UNSAFE.getDouble(offset);
    offset += 8;
    return v;
  }

  public int position() {
    return (int) (offset - baseOffset);
  }

  public ByteBufferReader position(int newPosition) {
    offset = baseOffset + newPosition;
    return this;
  }

  private static long getMemoryAddress(ByteBuffer buf) throws UnsupportedOperationException {
    long address;
    try {
      Field addressField = java.nio.Buffer.class.getDeclaredField("address");
      addressField.setAccessible(true);
      address = addressField.getLong(buf);
    } catch (Exception e) {
      throw new UnsupportedOperationException(e);
    }
    return address;
  }
}
