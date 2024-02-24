package com.cc.wheel.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cc
 * @date 2024/2/24
 */
@Slf4j
public class HashedWheelTimer implements Timer {

    /**
     * 时间轮工作者
     */
    private final class Worker implements Runnable {

        // 未处理的定时任务
        private final Set<Timeout> unprocessedTimeouts = new HashSet<>();

        // 时间轮的指针移动的刻度
        private long tick;

        @Override
        public void run() {
            startTime = System.nanoTime();
            if (startTime == 0) {
                // System.nanoTime()可能返回0，也可能是负数。
                // 这里用0来当作一个标识符。当startTime为0的时候，就把startTime赋值为1
                startTime = 1;
            }
            // 之前的线程可以继续向下运行了
            startTimeInitialized.countDown();
            do {
                // 返回的是work线程从开始工作到现在执行了多少时间
                final long deadline = waitForNextTick();
                if (deadline > 0) {
                    // 获取要执行的定时任务的那个数组下标。就是让指针当前的刻度和掩码做位运算
                    int idx = (int) (tick & mask);
                    //如果有任务已经被取消了，先把这些任务处理一下
                    processCancelledTasks();
                    // 上面已经得到了要执行的定时任务的数组下标，这里就可以得到该bucket
                    // bucket就是定时任务的一个双向链表，链表中的每个节点都是一个定时任务
                    HashedWheelBucket bucket = wheel[idx];
                    // 在真正执行定时任务之前，把即将被执行的任务从队列中放到时间轮的数组当中
                    transferTimeoutsToBuckets();
                    // 执行定时任务
                    bucket.expireTimeouts(deadline);
                    // 指针已经移动了，所以加1
                    tick++;
                }
                // 时间轮状态是开始执行状态就一直循环
            } while (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_STARTED);
            // 走到这里，说明时间轮的状态已经改变了
            // 遍历所有的bucket，还没被处理的定时任务都放到队列中
            for (HashedWheelBucket bucket : wheel) {
                bucket.clearTimeouts(unprocessedTimeouts);
            }
            for (; ; ) {
                //这里遍历的是任务队列中的任务，这些任务还没被放进时间轮数组中，将这些任务也都放进一个任务队列中
                HashedWheelTimeout timeout = timeouts.poll();
                if (Objects.isNull(timeout)) {
                    break;
                }
                if (!timeout.isCancelled()) {
                    unprocessedTimeouts.add(timeout);
                }
            }
            //如果有定时任务被取消了，在这里把它们从链表中删除
            processCancelledTasks();
        }

        /**
         * 把任务队列中的定时任务转移到时间轮的数组当中
         */
        private void transferTimeoutsToBuckets() {
            // 限制最多一次转移100000个，转移太多线程就干不了别的活了
            for (int i = 0; i < 100000; i++) {
                // 获取任务队列中的首个定时任务
                HashedWheelTimeout timeout = timeouts.poll();
                if (Objects.isNull(timeout)) {
                    break;
                }
                //如果该任务已经被取消了，就不转移该任务
                if (timeout.state() == HashedWheelTimeout.ST_CANCELLED) {
                    continue;
                }
                // 计算从worker线程开始运行算起要经过多少个tick，也就是刻度才能到这个任务
                long calculated = timeout.deadline / tickDuration;
                // 计算这个任务要经过多少圈，这里为什么要减tick，其实很简单，就是减去work线程已经走过的刻度
                timeout.remainingRounds = (calculated - tick) / wheel.length;
                // 通常calculated是大于tick的
                // 如果某些任务执行时间过长，导致tick大于calculated，此时直接把过时的任务放到当前链表队列
                final long ticks = Math.max(calculated, tick);
                // 位运算计算出该定时任务应该放在的数组下标
                int stopIndex = (int) (ticks & mask);
                // 得到数组下标中的bucket节点
                HashedWheelBucket bucket = wheel[stopIndex];
                // 把定时任务添加到链表之中
                bucket.addTimeout(timeout);
            }
        }

        /**
         * 处理已经被取消了的定时任务
         */
        private void processCancelledTasks() {
            // 这里没有数量限制，是考虑到取消的定时任务不会很多
            for (; ; ) {
                HashedWheelTimeout timeout = cancelledTimeouts.poll();
                if (Objects.isNull(timeout)) {
                    break;
                }
                try {
                    timeout.remove();
                } catch (Throwable t) {
                    log.warn("An exception was thrown while process a cancellation task", t);
                }
            }
        }

        /**
         * 阻塞线程到下一个刻度的工作时间
         *
         * @return work线程工作时间
         */
        private long waitForNextTick() {
            // 获取下一个刻度时间的和启动时间的时间差
            long deadline = tickDuration * (tick + 1);
            for (; ; ) {
                // work线程工作了多久的时间
                final long currentTime = System.nanoTime() - startTime;
                // 获取下一个刻度需要的睡眠时间
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;
                // 小于0则代表到了下一个刻度的执行时间
                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        // 返回work线程工作的时间
                        return currentTime;
                    }
                }
                try {
                    // 走到这里就意味着还没到执行时间，需要睡一会才行
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException ignored) {
                    //如果时间轮已经shutdown了，则返回MIN_VALUE
                    if (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_SHUTDOWN) {
                        return Long.MIN_VALUE;
                    }
                }
            }
        }

        public Set<Timeout> unprocessedTimeouts() {
            return Collections.unmodifiableSet(unprocessedTimeouts);
        }
    }

    // 时间轮限制
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private static final AtomicBoolean WARNED_TOO_MANY_INSTANCES = new AtomicBoolean();
    private static final int INSTANCE_COUNT_LIMIT = 64;

    // 时间轮工作状态原子更新器
    private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");

    // 时间轮工作状态
    public static final int WORKER_STATE_INIT = 0;
    public static final int WORKER_STATE_STARTED = 1;
    public static final int WORKER_STATE_SHUTDOWN = 2;
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private volatile int workerState;

    // 时间轮执行者
    private final Worker worker = new Worker();

    // workerThread 单线程用于处理所有的定时任务，它会在每个tick执行一个bucket中所有的定时任务和一些其他工作
    private final Thread workerThread;

    // tick时间间隔
    private final long tickDuration;

    // 掩码，计算定时任务要存入的数组下标
    private final int mask;

    // 时间轮数组，数组的每一个位置存放的是HashedWheelBucket类型的双向链表
    private final HashedWheelBucket[] wheel;

    // 最大的任务数量
    private final long maxPendingTimeouts;

    // 等待执行的定时任务的个数
    private final AtomicLong pendingTimeouts = new AtomicLong(0);

    // 启动时间
    private volatile long startTime;

    // 启动时间是否附值
    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);

    // 任务队列 还没放入时间轮
    private final Queue<HashedWheelTimeout> timeouts = new LinkedBlockingDeque<>();

    // 被取消的任务队列
    private final Queue<HashedWheelTimeout> cancelledTimeouts = new LinkedBlockingDeque<>();

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, long maxPendingTimeouts) {

        if (Objects.isNull(threadFactory)) {
            throw new NullPointerException("threadFactory");
        }
        if (Objects.isNull(unit)) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        // 创建时间轮的数组，数组的长度也是有讲究的，必须是不小于ticksPerWheel的最小2的n次方，这和hashmap中一样，用位运算求下标
        wheel = createWheel(ticksPerWheel);
        // 掩码，计算定时任务要存放的数组下标
        mask = wheel.length - 1;
        // 时间换算成纳秒
        long duration = unit.toNanos(tickDuration);
        // 时间间隔不能太长
        if (duration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", tickDuration, Long.MAX_VALUE / wheel.length));
        }
        long nano = TimeUnit.MILLISECONDS.toNanos(1);
        // 时间间隔不能太短，至少要大于1纳秒
        if (duration < nano) {
            log.warn("Configured tickDuration {} smaller then {}, using 1ms.", tickDuration, nano);
            this.tickDuration = nano;
        } else {
            this.tickDuration = duration;
        }
        //创建工作线程
        workerThread = threadFactory.newThread(worker);
        this.maxPendingTimeouts = maxPendingTimeouts;
        // 如果创建的时间轮对象超过64个，也会报警
        // 一个时间轮就是一个线程，线程太多也会影响性能
        if (INSTANCE_COUNTER.incrementAndGet() > INSTANCE_COUNT_LIMIT && WARNED_TOO_MANY_INSTANCES.compareAndSet(false, true)) {
            String resourceType = HashedWheelTimer.class.getSimpleName();
            log.error("You are creating too many " + resourceType + " instances. " + resourceType + " is a shared resource that must be reused across the JVM," + "so that only a few instances are created.");
        }
    }

    /**
     * @param ticksPerWheel 每个轮子的刻度
     * @return bucket 数组
     */
    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        //时间轮太小就抛出异常
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }
        // 把时间轮数组长度设定到2的次方
        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        //初始化每一个位置的bucket
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    /**
     * 长度设置到2的N次方
     */
    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = 1;
        while (normalizedTicksPerWheel < ticksPerWheel) {
            normalizedTicksPerWheel <<= 1;
        }
        return normalizedTicksPerWheel;
    }

    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        // 添加任务之后，等待执行的任务加1
        long pendingTimeoutsCount = pendingTimeouts.incrementAndGet();
        // 如果等待执行的任务超过时间轮能处理的最大任务数，就直接报错
        if (maxPendingTimeouts > 0 && pendingTimeoutsCount > maxPendingTimeouts) {
            pendingTimeouts.decrementAndGet();
            throw new IllegalArgumentException("Number of pending timeouts (" + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending " + "timeouts (" + maxPendingTimeouts + ")");
        }
        // 启动工作线程，并且确保只启动一次
        start();
        // 计算该定时任务的执行时间
        // startTime是worker线程的开始时间
        // 以后所有添加进来的任务的执行时间，都是根据这个开始时间做的对比
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        //检查时间间隔
        if (delay > 0 && deadline < 0) {
            deadline = Long.MAX_VALUE;
        }
        // 把提交的任务封装进一个HashedWheelTimeout中。
        HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
        // 将定时任务添加到任务队列中
        timeouts.add(timeout);
        return timeout;
    }

    @Override
    public Set<Timeout> stop() {
        // 判断当前线程是否是worker线程，不能让本身去停止本身
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(HashedWheelTimer.class.getSimpleName() + ".stop() cannot be called from " + TimerTask.class.getSimpleName());
        }
        // cas更新状态，如果更新不成功返回false，取反就是true，就会进入if分支
        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
                // 停止了一个时间轮，时间轮的个数就要减1
                INSTANCE_COUNTER.decrementAndGet();
            }
            return Collections.emptySet();
        }
        try {
            // 来到了这里，说明之前cas更新成功
            boolean interrupted = false;
            // while循环持续中断worker线程，isAlive用来判断该线程是否结束
            while (workerThread.isAlive()) {
                workerThread.interrupt();
                try {
                    // work线程阻塞的同时又被中断了，会抛出异常
                    workerThread.join(100);
                } catch (InterruptedException ignored) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                // 给执行该方法的线程设置中断标志位
                Thread.currentThread().interrupt();
            }
        } finally {
            // 减少实例数
            INSTANCE_COUNTER.decrementAndGet();
        }
        //返回还没执行的定时任务的集合
        return worker.unprocessedTimeouts();
    }


    /**
     * 启动时间轮的方法
     */
    public void start() {
        // 判断时间轮的工作状态
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                // cas更新到开始状态
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    // 启动work线程
                    // 该线程一旦启动，就会执行任务，核心在work线程要执行的runnable的run方法内
                    workerThread.start();
                }
                break;
            // 如果启动了就什么也不做
            case WORKER_STATE_STARTED:
                break;
            // 如果状态是结束，就抛出异常
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }
        // 这里会暂停一卡，因为要等待work线程启动完全，并且startTime被赋值成功
        while (startTime == 0) {
            try {
                startTimeInitialized.await();
            } catch (InterruptedException ignore) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    /**
     * 记录取消的timeout
     *
     * @param timeout timeout
     */
    void addCanceledTimeouts(HashedWheelTimeout timeout) {
        this.cancelledTimeouts.add(timeout);
    }

    /**
     * 减少等待执行的定时任务个数
     */
    void decrementPendingTimeouts() {
        this.pendingTimeouts.decrementAndGet();
    }

    /**
     * @return 等待执行的定时任务个数
     */
    public long pendingTimeouts() {
        return pendingTimeouts.get();
    }
}
