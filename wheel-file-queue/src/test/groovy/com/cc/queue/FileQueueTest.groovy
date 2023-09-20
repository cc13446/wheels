package com.cc.queue

import com.cc.queue.impl.FileQueueImpl
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

        Thread.sleep(1000 * 10)

        then:
        res.get() == 0

    }
}