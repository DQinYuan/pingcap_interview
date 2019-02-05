package org.du.interview.pingcap.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectiveUtil {

    //Bundle reflection calls to get access to the given method
    public static Method getMethod(Class<?> cls, String name, Class<?>... params) throws NoSuchMethodException {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    public static Field getField(Class<?> cls, String name) throws NoSuchFieldException {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... params) throws NoSuchMethodException {
        Constructor<T> constructor = cls.getDeclaredConstructor(params);
        constructor.setAccessible(true);
        return constructor;
    }
}
