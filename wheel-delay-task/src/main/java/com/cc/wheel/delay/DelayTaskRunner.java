package com.cc.wheel.delay;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 运行延迟任务的工具类
 * @author cc
 * @date 2023/8/23
 */
@Slf4j
public class DelayTaskRunner implements Runnable {

    private final int corePoolSize;

    private final int maxPoolSize;

    private final int keepAliveTime;

    private final int workQueueSize;

    /**
     * 定义节流时间
     */
    private final Map<String, Long> throttleMap = new ConcurrentHashMap<>();

    /**
     * 缓存上次运行时间
     */
    private final Map<String, Long> preTimeMap = new ConcurrentHashMap<>();

    /**
     * 延迟队列
     */
    private final DelayQueue<DelayTask<?>> delayTaskQueue = new DelayQueue<>();

    public DelayTaskRunner() {
        this.corePoolSize = 4;
        this.maxPoolSize = 16;
        this.keepAliveTime = 60;
        this.workQueueSize = 128;
    }

    public DelayTaskRunner(int corePoolSize, int maxPoolSize, int keepAliveTime, int workQueueSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.workQueueSize = workQueueSize;
    }

    /**
     * @param namespace 命名空间
     * @param second 节流秒数
     */
    public void putThrottle(String namespace, long second) {
        this.throttleMap.put(namespace, TimeUnit.SECONDS.toMillis(second));
    }

    /**
     * @param namespace 命名空间
     * @return 节流秒数
     */
    public long removeThrottle(String namespace) {
        return this.throttleMap.remove(namespace);
    }

    /**
     * @param task 任务
     * @param <T> 任务结果
     * @return 任务结果 future
     */
    public <T> ListenableFuture<T> putTask(DelayTask<T> task) {
        this.delayTaskQueue.put(task);
        return task.getFuture();
    }

    /**
     * @param nameSpace 命名空间
     * @param id id
     */
    public void removeTask(String nameSpace, String id) {
        DelayTask<Void> delete = new DelayTask<>(nameSpace, id, 0L, null);
        Iterator<DelayTask<?>> iterator = this.delayTaskQueue.iterator();
        while (iterator.hasNext()) {
            DelayTask<?> delayTask = iterator.next();
            if (delayTask.equals(delete)) {
                iterator.remove();
                delayTask.getFuture().cancel(true);
            }
        }
    }

    /**
     * 防抖任务
     * @param task 任务
     * @param <T> 任务结果
     * @return 任务结果 future
     */
    public <T> ListenableFuture<T> putAntiShakeTask(DelayTask<T> task) {
        removeTask(task.getNameSpace(), task.getId());
        return putTask(task);
    }

    private <T> void doTask(DelayTask<T> task, ThreadPoolExecutor executor) {
        long throttleTime = throttleMap.getOrDefault(task.getNameSpace(), 0L);
        long preTime = preTimeMap.getOrDefault(task.getKey(), 0L);
        long now = System.currentTimeMillis();
        if(now - preTime >= throttleTime) {
            // 做任务
            if (Objects.isNull(task.getTask())) {
                task.getFuture().setException(new RuntimeException("The task is null"));
                return;
            }
            Future<T> future = executor.submit(task.getTask());
            ListenableFuture<T> listenableFuture = JdkFutureAdapters.listenInPoolThread(future);
            task.getFuture().setFuture(listenableFuture);
            preTimeMap.put(task.getKey(), now);
        } else {
            // 节流 取消任务
            task.getFuture().cancel(true);
        }
    }

    @Override
    public void run() {
        ThreadFactory factory = new BasicThreadFactory.Builder().daemon(true).namingPattern("Delay-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(workQueueSize), factory);
        executor.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    DelayTask<?> task = delayTaskQueue.take();
                    doTask(task, executor);
                } catch (Exception e) {
                    log.error("Run delay task error", e);
                }
            }
            executor.shutdown();
        });

    }
}
