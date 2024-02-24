package com.cc.wheel.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 封装了用户提交的定时任务，而且该类的对象构成了bucket的双向链表
 *
 * @author cc
 * @date 2024/2/24
 */
@Slf4j
public class HashedWheelTimeout implements Timeout {

    // 定时任务状态
    static final int ST_INIT = 0;
    static final int ST_CANCELLED = 1;
    static final int ST_EXPIRED = 2;
    private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");

    // 定时任务状态
    @SuppressWarnings({"FieldMayBeFinal"})
    private volatile int state = ST_INIT;

    // 定时器
    final HashedWheelTimer timer;

    // 定时任务
    final TimerTask task;

    // 超时时间
    final long deadline;

    // 定时任务的轮数
    long remainingRounds;

    // 双向链表结构，worker单线程访问
    HashedWheelTimeout next;
    HashedWheelTimeout prev;

    // 定时任务所在的时间轮数组
    HashedWheelBucket bucket;

    HashedWheelTimeout(HashedWheelTimer timer, TimerTask task, long deadline) {
        this.timer = timer;
        this.task = task;
        this.deadline = deadline;
    }

    @Override
    public Timer timer() {
        return timer;
    }

    @Override
    public TimerTask task() {
        return task;
    }

    @Override
    public boolean isExpired() {
        return state() == ST_EXPIRED;
    }

    @Override
    public boolean isCancelled() {
        return state() == ST_CANCELLED;

    }

    @Override
    public boolean cancel() {
        if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
            return false;
        }
        // 记录取消的任务
        timer.addCanceledTimeouts(this);
        return true;
    }

    /**
     * 定时任务超时
     */
    void expire() {
        if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
            return;
        }
        try {
            task.run(this);
        } catch (Throwable t) {
            log.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t);
        }
    }

    /**
     * 移除定时任务
     */
    void remove() {
        HashedWheelBucket bucket = this.bucket;
        if (Objects.nonNull(bucket)) {
            bucket.remove(this);
        } else {
            timer.decrementPendingTimeouts();
        }
    }

    /**
     * @param expected 期待的状态
     * @param state    新状态
     * @return 是否修改成功
     */
    boolean compareAndSetState(int expected, int state) {
        return STATE_UPDATER.compareAndSet(this, expected, state);
    }

    /**
     * @return 当前定时任务状态
     */
    int state() {
        return state;
    }
}
