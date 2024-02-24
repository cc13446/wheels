import com.cc.wheel.timer.HashedWheelTimer
import com.cc.wheel.timer.Timeout
import com.cc.wheel.timer.TimerTask
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
/**
 * @author cc
 * @date 2024/2/24
 */
class HashedWheelTimerTest extends Specification{

    def "test hashed wheel timer" () {
        given:
        def result = []
        def target = new HashedWheelTimer(Executors.defaultThreadFactory(), 1, TimeUnit.MILLISECONDS, 128, 65535)
        def countAndDown = new CountDownLatch(1)
        def one = new TimerTask() {
            @Override
            void run(Timeout timeout) throws Exception {
                result.add(1)
            }
        }
        def two = new TimerTask() {
            @Override
            void run(Timeout timeout) throws Exception {
                result.add(2)
            }
        }
        def three = new TimerTask() {
            @Override
            void run(Timeout timeout) throws Exception {
                result.add(3)
            }
        }
        def finish = new TimerTask() {
            @Override
            void run(Timeout timeout) throws Exception {
                result.add(0)
                countAndDown.countDown()
            }
        }
        when:
        target.newTimeout(one, 1, TimeUnit.SECONDS)
        target.newTimeout(two, 2, TimeUnit.SECONDS)
        target.newTimeout(three, 3, TimeUnit.SECONDS)
        target.newTimeout(two, 4, TimeUnit.SECONDS)
        target.newTimeout(three, 5, TimeUnit.SECONDS).cancel()
        target.newTimeout(finish, 5, TimeUnit.SECONDS)
        countAndDown.await()
        then:
        result == [1, 2, 3, 2, 0]
        target.stop().size() == 0

    }
}
