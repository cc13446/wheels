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
class DelayTaskAntiTest extends Specification {

    private def delayTaskRunner = new DelayTaskRunner()

    private def listeningExecutor = Executors.newSingleThreadExecutor()
    def "test anti shake task"() {
        given:
        listeningExecutor.execute(delayTaskRunner)
        String namespace = "ns"
        String id = "id"
        ListenableFuture<Integer> four = delayTaskRunner.putTask(new DelayTask(namespace, id, 4L, () -> 4))
        ListenableFuture<Integer> third = delayTaskRunner.putTask(new DelayTask(namespace, id, 3L, () -> 3))
        ListenableFuture<Integer> two = delayTaskRunner.putTask(new DelayTask(namespace, id, 2L, () -> 2))
        ListenableFuture<Integer> one = delayTaskRunner.putAntiShakeTask(new DelayTask(namespace, id, 1L, () -> 1))

        when:
        def res = []

        def callback = new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                res.add(result)
            }

            @Override
            void onFailure(Throwable t) {
                log.error("Anti call back error ${t.getCause()} ")
            }
        }

        Futures.addCallback(third, callback, listeningExecutor)
        Futures.addCallback(two, callback, listeningExecutor)
        Futures.addCallback(one, callback, listeningExecutor)
        Futures.addCallback(four, callback, listeningExecutor)

        one.get()

        then:
        res[0] == 1

        when:
        four.get()

        then:
        thrown(CancellationException)
        res.size() == 1
    }

}