package com.cc.wheel.queue.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author cc
 * @date 2023/9/20
 */
public class ThreadPoolUtils {

    public static final Executor INS;

    static {
        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("File-Queue-%d")
                .setDaemon(true)
                .setPriority(1)
                .build();
        INS = new ThreadPoolExecutor(
                2,
                2,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(128),
                factory);
    }
}
