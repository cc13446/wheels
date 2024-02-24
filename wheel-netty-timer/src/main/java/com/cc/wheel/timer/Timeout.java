package com.cc.wheel.timer;

/**
 * @author cc
 * @date 2024/2/24
 */
public interface Timeout {

    /**
     * @return Timer
     */
    Timer timer();

    /**
     * @return TimerTask
     */
    TimerTask task();

    /**
     * @return 是否超时
     */
    boolean isExpired();

    /**
     * @return 是否取消
     */
    boolean isCancelled();

    /**
     * @return 是否取消成功
     */
    boolean cancel();
}
