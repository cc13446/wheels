package com.cc.wheel.ring;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author cc
 * @date 2023/10/5
 */
@Slf4j
@SuppressWarnings("unused")
public class RingBuffer<T> {

    private static final Unsafe UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = () -> {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                return (Unsafe) theUnsafe.get(null);
            };
            UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    private static final int BUFFER_BASE_OFFSET;
    private static final int BUFFER_ELEMENT_SHIFT;
    private static final int BUFFER_PADDING_SHIFT;

    private static final long WRITE_INDEX_OFFSET;
    private static final long READ_INDEX_OFFSET;

    static {
        try {
            final int scale = UNSAFE.arrayIndexScale(Object[].class);
            // 每个element的偏移量
            BUFFER_ELEMENT_SHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            // 伪共享填充的偏移量
            BUFFER_PADDING_SHIFT = 31 - Integer.numberOfLeadingZeros(64 / scale);
            // 数组的基地址 包括数组前面的填充
            BUFFER_BASE_OFFSET = UNSAFE.arrayBaseOffset(Object[].class) + (1 << BUFFER_ELEMENT_SHIFT << BUFFER_PADDING_SHIFT);

            READ_INDEX_OFFSET = UNSAFE.objectFieldOffset(RingBuffer.class.getDeclaredField("readIndex"));
            WRITE_INDEX_OFFSET = UNSAFE.objectFieldOffset(RingBuffer.class.getDeclaredField("writeIndex"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final int indexMask;

    private long l1, l2, l3, l4, l5, l6, l7;

    private volatile long readIndex;

    private final Object[] buffer;

    private volatile long writeIndex;

    private long l9, l10, l11, l12, l13, l14, l15;

    public RingBuffer(final int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        this.indexMask = bufferSize - 1;
        // 包括了element之间的填充
        this.buffer = new Object[(bufferSize + 1) << BUFFER_PADDING_SHIFT];

    }

    private boolean bufferCompareNullAndSwap(final long index, Object value) {
        return UNSAFE.compareAndSwapObject(this.buffer, BUFFER_BASE_OFFSET + (index << BUFFER_ELEMENT_SHIFT << BUFFER_PADDING_SHIFT), null, value);
    }

    private Object bufferGetAndSetNull(final long index) {
        return UNSAFE.getAndSetObject(this.buffer, BUFFER_BASE_OFFSET + (index << BUFFER_ELEMENT_SHIFT << BUFFER_PADDING_SHIFT), null);
    }

    private long objectGetAndIncrementLong(final long offset) {
        return UNSAFE.getAndAddLong(this, offset, 1);
    }

    public void put(@NonNull T value) throws InterruptedException {
        final long writeIndex = objectGetAndIncrementLong(WRITE_INDEX_OFFSET) & indexMask;
        while (!Thread.interrupted()) {
            if (bufferCompareNullAndSwap(writeIndex, value)) {
                return;
            }
            Thread.yield();
        }
        throw new InterruptedException();
    }

    @SuppressWarnings("unchecked")
    public @NonNull T take() throws InterruptedException {
        final long readIndex = objectGetAndIncrementLong(READ_INDEX_OFFSET) & indexMask;
        while (!Thread.interrupted()) {
            final Object result;
            if (Objects.nonNull(result = bufferGetAndSetNull(readIndex))) {
                return (T) result;
            }
            Thread.yield();

        }
        throw new InterruptedException();
    }

    // do test
    private static final String S = "TEST";
    private final static long ITERATIONS = 2L * 3L * 1000L * 1000L * 5L;
    private final static ArrayBlockingQueue<String> compare = new ArrayBlockingQueue<>(1 << 5);
    private final static RingBuffer<String> target = new RingBuffer<>(1 << 5);

    public static void main(String[] args) throws InterruptedException {
        final int MAX_THREAD = 6;

        System.gc();
        long start = System.currentTimeMillis();
        doTest(1, 1, false);
        log.info("The compare cost: {} when thread {}", System.currentTimeMillis() - start, "1-1");
        System.gc();
        start = System.currentTimeMillis();
        doTest(1, 1, true);
        log.info("The ring buffer cost: {} when thread {}", System.currentTimeMillis() - start, "1-1");

        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc();
            start = System.currentTimeMillis();
            doTest(i, MAX_THREAD - i, false);
            log.info("The compare cost: {} when thread {}-{}", System.currentTimeMillis() - start, i, MAX_THREAD - i);
        }
        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc();
            start = System.currentTimeMillis();
            doTest(i, MAX_THREAD - i, true);
            log.info("The ring buffer cost: {} when thread {}-{}", System.currentTimeMillis() - start, i, MAX_THREAD - i);
        }
    }

    public static void doTest(final int NUM_P_THREADS, final int NUM_C_THREADS, boolean isTarget) throws InterruptedException {
        Thread[] pThreads = new Thread[NUM_P_THREADS];
        Thread[] cThreads = new Thread[NUM_C_THREADS];
        final long write = ITERATIONS / NUM_P_THREADS;
        final long read = ITERATIONS / NUM_C_THREADS;
        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < write; j++) {
                        if (isTarget) {
                            target.put(S);
                        } else {
                            compare.put(S);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
        }
        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < read; j++) {

                        if (isTarget) {
                            String s = target.take();
                        } else {
                            String s = compare.take();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
        }
        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i].start();
        }

        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i].start();
        }

        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i].join();
        }

        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i].join();
        }
    }
}
