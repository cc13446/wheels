package com.cc.wheel.delay;

import com.google.common.util.concurrent.SettableFuture;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author cc
 * @date 2023/8/23
 */
public class DelayTask<T> implements Delayed {

    private final String nameSpace;

    private final String id;

    private final long time;

    private final Callable<T> task;

    private final SettableFuture<T> future;

    public DelayTask(String nameSpace, String id, long time, Callable<T> task) {
        this.nameSpace = nameSpace;
        this.id = id;
        this.time = TimeUnit.SECONDS.toMillis(time) + System.currentTimeMillis();
        this.task = task;
        this.future = SettableFuture.create();
    }

    String getNameSpace() {
        return nameSpace;
    }

    String getId() {
        return id;
    }

    String getKey() {
        return nameSpace + "-" + id;
    }

    Callable<T> getTask() {
        return task;
    }

    SettableFuture<T> getFuture() {
        return future;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DelayTask)) {
            return false;
        }
        DelayTask<?> task1 = (DelayTask<?>) o;
        return Objects.equals(nameSpace, task1.nameSpace) && Objects.equals(id, task1.id);

    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSpace, id);
    }
}
