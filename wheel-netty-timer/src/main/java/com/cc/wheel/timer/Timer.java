package com.cc.wheel.timer;

import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * @author cc
 * @date 2024/2/24
 */
public interface Timer {

    /**
     * @param task  任务
     * @param delay 延迟
     * @param unit  单位
     * @return Timeout
     */
    Timeout newTimeout(TimerTask task, long delay, TimeUnit unit);


    /**
     * 停止Timer
     *
     * @return 未完成的任务
     */
    Set<Timeout> stop();
}