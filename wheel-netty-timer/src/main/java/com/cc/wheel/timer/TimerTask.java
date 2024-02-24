package com.cc.wheel.timer;

/**
 * @author cc
 * @date 2024/2/24
 */
public interface TimerTask {
    /**
     * @param timeout timeout
     * @throws Exception 异常
     */
    void run(Timeout timeout) throws Exception;
}
