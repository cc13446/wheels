package com.cc.wheel.queue

import com.cc.wheel.queue.impl.FileQueueImpl
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
/**
 * @author cc
 * @date 2023/9/20
 */
@Slf4j
class FileQueueTest extends Specification{

    def "test file queue"() {
        given:
        FileQueue fileQueue = new FileQueueImpl()
        Executor executor = Executors.newCachedThreadPool()
        def size = 10 * 128 * 1024L
        def res = new AtomicLong(size)

        when:
        def futures = new ArrayList<Future<Long>>()
        for (int i = 0; i < 10; i++) {
            Callable<Long> task = new Callable<Long>() {
                @Override
                Long call() throws Exception {
                    for (int j = 0; j < size / 10; j++) {
                        fileQueue.put(UUID.randomUUID().toString())
                    }
                    return 0L
                }
            }
            futures.add(executor.submit(task))
        }

        for (int i = 0; i < 10; i++) {
            Callable<Long> task = new Callable<Long>() {
                @Override
                Long call() throws Exception {
                    while(!Thread.interrupted()) {
                        fileQueue.take()
                        res.getAndDecrement()
                    }
                    return 0L
                }
            }
            futures.add(executor.submit(task))
        }
        for (def i = 0; i < 20 && res.get() > 0; i++) {
            Thread.sleep(1000)
        }

        then:
        res.get() == 0

    }
}
