package com.cc.wheel.ring


import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue

/**
 * @author cc
 * @date 2023/10/5
 */
@Slf4j
class RingBufferTest extends Specification {
    private static final String S = "TEST"
    private final static long ITERATIONS = 2L * 3L * 1000L * 1000L * 3L

    def "test ring buffer 1-1"() {
        given:
        def compare = new ArrayBlockingQueue<String>(1 << 10)
        def target = new RingBuffer<String>(1 << 10)

        when:
        System.gc()
        long start = System.currentTimeMillis()
        doTest(1, 1, compare)
        log.info("The compare cost: {} when thread {}", System.currentTimeMillis() - start, "1-1")
        System.gc()
        start = System.currentTimeMillis()
        doTest(1, 1, target)
        log.info("The ring buffer cost: {} when thread {}", System.currentTimeMillis() - start, "1-1")

        then:
        // do nothing
        true

    }

    def "test ring buffer n-m"() {
        given:
        def compare = new ArrayBlockingQueue<String>(1 << 10)
        def target = new RingBuffer<String>(1 << 10)
        final int MAX_THREAD = 4

        when:
        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc()
            long start = System.currentTimeMillis()
            doTest(i, MAX_THREAD - i, compare)
            log.info("The compare cost: {} when thread {}-{}", System.currentTimeMillis() - start, i, MAX_THREAD - i)
        }
        for (int i = 1; i < MAX_THREAD; i++) {
            System.gc()
            long start = System.currentTimeMillis()
            doTest(i, MAX_THREAD - i, target)
            log.info("The ring buffer cost: {} when thread {}-{}", System.currentTimeMillis() - start, i, MAX_THREAD - i)
        }

        then:
        // do nothing
        true

    }

    def doTest(final int NUM_P_THREADS, final int NUM_C_THREADS, Object target) {
        Thread[] pThreads = new Thread[NUM_P_THREADS]
        Thread[] cThreads = new Thread[NUM_C_THREADS]
        final write = ITERATIONS / NUM_P_THREADS
        final read = ITERATIONS / NUM_C_THREADS
        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i] = new Thread(() -> {
                for (int j = 0; j < write; j++) {
                    target.put(S)
                }
            })
        }
        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i] = new Thread(() -> {
                for (int j = 0; j < read; j++) {
                    target.take()
                }
            })
        }
        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i].start()
        }

        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i].start()
        }

        for (int i = 0; i < NUM_P_THREADS; i++) {
            pThreads[i].join()
        }

        for (int i = 0; i < NUM_C_THREADS; i++) {
            cThreads[i].join()
        }
    }
}
