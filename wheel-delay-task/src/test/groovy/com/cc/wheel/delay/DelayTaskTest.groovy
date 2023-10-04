package com.cc.wheel.delay

import com.cc.wheel.delay.DelayTask
import com.cc.wheel.delay.DelayTaskRunner
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.CancellationException
import java.util.concurrent.Executors

/**
 * @author cc
 * @date 2023/8/23
 */
@Slf4j
class DelayTaskTest extends Specification {

    private def delayTaskRunner = new DelayTaskRunner()

    private def listeningExecutor = Executors.newSingleThreadExecutor()

    def "test delay task"() {
        given:
        listeningExecutor.execute(delayTaskRunner)
        String namespace = "ns"
        String id = "id"
        ListenableFuture<Integer> four = delayTaskRunner.putTask(new DelayTask(namespace, id, 4L, () -> 4))
        ListenableFuture<Integer> third = delayTaskRunner.putTask(new DelayTask(namespace, id, 3L, () -> 3))
        ListenableFuture<Integer> two = delayTaskRunner.putTask(new DelayTask(namespace, id, 2L, () -> 2))
        ListenableFuture<Integer> one = delayTaskRunner.putTask(new DelayTask(namespace, id, 1L, () -> 1))

        when:
        def res = []

        def callback = new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                res.add(result)
            }

            @Override
            void onFailure(Throwable t) {
                log.error("Call back error ${t.getCause()}")
            }
        }

        Futures.addCallback(third, callback, listeningExecutor)
        Futures.addCallback(two, callback, listeningExecutor)
        Futures.addCallback(one, callback, listeningExecutor)
        Futures.addCallback(four, callback, listeningExecutor)

        four.get()
        then:
        res[0] == 1
        res[1] == 2
        res[2] == 3
        res[3] == 4
    }

    def "test remove task"() {
        given:
        listeningExecutor.execute(delayTaskRunner)
        String namespace = "ns"
        String id = "id"
        ListenableFuture<Integer> four = delayTaskRunner.putTask(new DelayTask(namespace, id, 4L, () -> 4))
        ListenableFuture<Integer> third = delayTaskRunner.putTask(new DelayTask(namespace, id, 3L, () -> 3))
        ListenableFuture<Integer> two = delayTaskRunner.putTask(new DelayTask(namespace, id, 2L, () -> 2))
        ListenableFuture<Integer> one = delayTaskRunner.putTask(new DelayTask(namespace, id, 1L, () -> 1))

        when:
        def res = []

        def callback = new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                res.add(result)
            }

            @Override
            void onFailure(Throwable t) {
                log.error("Remove call back error ${t.getCause()}")
            }
        }

        Futures.addCallback(third, callback, listeningExecutor)
        Futures.addCallback(two, callback, listeningExecutor)
        Futures.addCallback(one, callback, listeningExecutor)
        Futures.addCallback(four, callback, listeningExecutor)

        delayTaskRunner.removeTask(namespace, id)

        one.get()
        then:
        thrown(CancellationException)
        res.size() == 0
    }

    def "test throttle task"() {
        given:
        listeningExecutor.execute(delayTaskRunner)
        String namespace = "ns"
        String id = "id"
        delayTaskRunner.putThrottle(namespace, 3L)
        ListenableFuture<Integer> third = delayTaskRunner.putTask(new DelayTask(namespace, id, 3L, () -> 3))
        ListenableFuture<Integer> two = delayTaskRunner.putTask(new DelayTask(namespace, id, 2L, () -> 2))
        ListenableFuture<Integer> one = delayTaskRunner.putTask(new DelayTask(namespace, id, 1L, () -> 1))
        ListenableFuture<Integer> four = delayTaskRunner.putTask(new DelayTask(namespace, id, 8L, () -> 4))

        when:
        def res = []

        def callback = new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                res.add(result)
            }

            @Override
            void onFailure(Throwable t) {
                log.error("Throttle call back error ${t.getCause()}")
            }
        }

        Futures.addCallback(third, callback, listeningExecutor)
        Futures.addCallback(two, callback, listeningExecutor)
        Futures.addCallback(one, callback, listeningExecutor)
        Futures.addCallback(four, callback, listeningExecutor)

        one.get()
        four.get()

        then:
        res[0] == 1
        res[1] == 4

        when:
        two.get()

        then:
        thrown(CancellationException)
        res.size() == 2
    }
}
