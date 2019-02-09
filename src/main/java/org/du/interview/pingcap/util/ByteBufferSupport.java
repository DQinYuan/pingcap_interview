package org.du.interview.pingcap.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ByteBufferSupport
{
    private static final MethodHandle INVOKE_CLEANER;

    static {
        MethodHandle invoker;
        try {
            // Java 9 added an invokeCleaner method to Unsafe to work around
            // module visibility issues for code that used to rely on DirectByteBuffer's cleaner()
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            invoker = MethodHandles.lookup()
                    .findVirtual(unsafeClass, "invokeCleaner", MethodType.methodType(void.class, ByteBuffer.class))
                    .bindTo(theUnsafe.get(null));
        }
        catch (Exception e) {
            // fall back to pre-java 9 compatible behavior
            try {
                Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");

                Method cleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner");
                cleanerMethod.setAccessible(true);
                MethodHandle getCleaner = MethodHandles.lookup().unreflect(cleanerMethod);

                Method cleanMethod = cleanerClass.getDeclaredMethod("clean");
                cleanerMethod.setAccessible(true);
                MethodHandle clean = MethodHandles.lookup().unreflect(cleanMethod);

                clean = MethodHandles.dropArguments(clean, 1, directByteBufferClass);
                invoker = MethodHandles.foldArguments(clean, getCleaner);
            }
            catch (Exception e1) {
                throw new AssertionError(e1);
            }
        }
        INVOKE_CLEANER = invoker;
    }

    private ByteBufferSupport()
    {
    }

    public static void unmap(MappedByteBuffer buffer)
    {
        try {
            INVOKE_CLEANER.invoke(buffer);
        }
        catch (Throwable ignored) {
            System.err.println("unmap出现问题");
        }
    }

    private static void splitUtil(ByteBuffer origin, int splitNum, int offset,
                                  BiConsumer<ByteBuffer, Integer> consumer, int until){
        int capacity = origin.capacity();
        //System.out.println("init capacity:" + capacity);
        int blockSize = (capacity - offset) / splitNum;
        //System.out.println("block size:" + blockSize);
        int i = 0;
        for ( ; offset < capacity - blockSize * until; offset += blockSize){
            //System.out.println("offset:" + offset);
            origin.position(offset);
            origin.limit(offset + blockSize);
            ByteBuffer split = origin.slice();
            consumer.accept(split, i);
            i++;
        }

        //System.out.println("last offset:" + offset);

        origin.position(offset);
        origin.limit(origin.capacity());
    }

    public static void splitExceptLast(ByteBuffer origin, int splitNum, int offset,
                                       BiConsumer<ByteBuffer, Integer> consumer){
        splitUtil(origin, splitNum, offset, consumer, 1);
    }

    public static void split(ByteBuffer origin, int splitNum, int offset,
                             BiConsumer<ByteBuffer, Integer> consumer){
        splitUtil(origin, splitNum, offset, consumer, 0);
    }


    public static ByteBuffer[] split(ByteBuffer origin, int blockSize){
        int capacity = origin.capacity();
        ByteBuffer[] result = new ByteBuffer[capacity / blockSize];
        for (int i = 0; i < capacity; i += blockSize){
            origin.position(i);
            origin.limit(i + blockSize);
            ByteBuffer split = origin.slice();
            result[i / blockSize] = split;
        }

        return result;
    }

}
