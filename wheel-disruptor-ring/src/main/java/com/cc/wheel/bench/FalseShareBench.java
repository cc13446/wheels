package com.cc.wheel.bench;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FalseShareBench {
    public final static long ITERATIONS = 500L * 1000L * 100L;

    private static final Object OBJECT = new Object();

    public static void main(final String[] args) throws Exception {
        final int MAX_THREAD = 10;

        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc();
            final long start = System.currentTimeMillis();
            runTestNoPadding(i);
            log.info("NoPadding Thread num " + i + " duration = " + (System.currentTimeMillis() - start));
        }

        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc();
            final long start = System.currentTimeMillis();
            runTestPadding(i);
            log.info("Padding Thread num " + i + " duration = " + (System.currentTimeMillis() - start));
        }

    }

    private static void runTestPadding(final int NUM_THREADS) throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];
        final Padding[] paddings = new Padding[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            paddings[i] = new Padding();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (long count = ITERATIONS + 1; 0 != --count; ) {
                    paddings[index].value = OBJECT;
                }
            });
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private static void runTestNoPadding(int NUM_THREADS) throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];
        final NoPadding[] noPaddings = new NoPadding[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            noPaddings[i] = new NoPadding();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (long count = ITERATIONS + 1; 0 != --count; ) {
                    noPaddings[index].value = OBJECT;
                }
            });
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }
}