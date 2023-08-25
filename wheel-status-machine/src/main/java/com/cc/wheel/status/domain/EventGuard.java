package com.cc.wheel.status.domain;

import java.util.Map;

/**
 * @author cc
 * @date 2023/8/19
 */
public interface EventGuard extends RankCall {

    /**
     * 转移守卫
     * @param from 从哪个状态来
     * @param to 往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     * @return 回调是否转移成功
     */
    boolean tryTrans(String from, String to, String event, Map<String, Object> args);
}
