package org.du.interview.pingcap.util;

import java.lang.reflect.Field;


public class MyUnsafe {

  public static sun.misc.Unsafe UNSAFE;

  public static long BYTE_ARRAY_BASE_OFFSET;

  static {
    try {
      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      UNSAFE = (sun.misc.Unsafe) unsafeField.get(null);
      BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    } catch (NoSuchFieldException e) {
      UNSAFE = null;
    } catch (IllegalAccessException e) {
      UNSAFE = null;
    }
  }
}
