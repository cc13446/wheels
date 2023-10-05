package com.cc.wheel.ring;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cc
 * @date 2023/10/5
 */
public class RingBuffer<T> {

    @SuppressWarnings("unused")
    private final static class Buffer<T> {
        private long p1, p2, p3, p4, p5, p6, p7;
        private volatile T value;
        private long p9, p10, p11, p12, p13, p14, p15;
    }


    @SuppressWarnings("unused")
    private final static class PaddingAtomicLong extends AtomicLong {
        public volatile long p1, p2, p3, p4, p5, p6 = 7L;
    }


    private final int bufferSize;

    private final int indexMask;

    private final Buffer<T>[] buffer;

    private final PaddingAtomicLong size = new PaddingAtomicLong();

    private final PaddingAtomicLong readIndex = new PaddingAtomicLong();

    private final PaddingAtomicLong writeIndex = new PaddingAtomicLong();

    @SuppressWarnings("unchecked")
    public RingBuffer(final int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        this.bufferSize = bufferSize;
        this.indexMask = bufferSize - 1;
        this.buffer = (Buffer<T>[]) Array.newInstance(Buffer.class, bufferSize);

        for (int i = 0; i < this.bufferSize; i++) {
            buffer[i] = new Buffer<>();
        }
    }

    public void put(T value) throws InterruptedException {
        while (!Thread.interrupted()) {
            final long s;
            if ((s = size.get()) < this.bufferSize) {
                if (size.compareAndSet(s, s + 1)) {
                    final int writeIndex = (int) (this.writeIndex.getAndIncrement() & indexMask);
                    while (!Thread.interrupted()) {
                        if (Objects.isNull(buffer[writeIndex].value)) {
                            buffer[writeIndex].value = value;
                            return;
                        }
                        Thread.yield();
                    }
                }
            }
            Thread.yield();
        }
        throw new InterruptedException();
    }

    public T take() throws InterruptedException {
        while (!Thread.interrupted()) {
            final long s;
            if ((s = size.get()) > 0) {
                if (size.compareAndSet(s, s - 1)) {
                    final int readIndex = (int) (this.readIndex.getAndIncrement() & indexMask);
                    while (!Thread.interrupted()) {
                        if (Objects.nonNull(buffer[readIndex].value)) {
                            final T result = buffer[readIndex].value;
                            buffer[readIndex].value = null;
                            return result;
                        }
                        Thread.yield();

                    }
                }
            }
            Thread.yield();

        }
        throw new InterruptedException();
    }

}
