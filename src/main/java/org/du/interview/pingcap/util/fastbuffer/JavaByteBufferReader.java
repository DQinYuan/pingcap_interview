package org.du.interview.pingcap.util.fastbuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * An implementation of the ByteBufferReader using methods directly from ByteBuffer.
 * This is used as the fallback mode in case Unsafe is not supported.
 */
public class JavaByteBufferReader implements ByteBufferReader {

  private ByteBuffer buf;

  public JavaByteBufferReader(ByteBuffer buf) {
    this.buf = buf.duplicate();
    this.buf.order(ByteOrder.nativeOrder());
  }

  public byte getByte() {
    return buf.get();
  }

  public byte[] getBytes(byte[] dst, int len) {
    buf.get(dst, 0, len);
    return dst;
  }

  public short getShort() {
    return buf.getShort();
  }

  public int getInt() {
    return buf.getInt();
  }

  public long getLong() {
    return buf.getLong();
  }

  public float getFloat() {
    return buf.getFloat();
  }

  public double getDouble() {
    return buf.getDouble();
  }

  public int position() {
    return buf.position();
  }

  public ByteBufferReader position(int newPosition) {
    buf.position(newPosition);
    return this;
  }
}
