package com.cc.wheel.status.domain;

import java.util.Map;

/**
 * @author cc
 * @date 2023/8/19
 */
@FunctionalInterface
public interface EntryCall extends RankCall {

    /**
     * 入口回调
     *
     * @param from  从哪个状态来
     * @param to    往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     * @return 入口自动触发的事件，没有则返回null, 最后一个有效。
     */
    String entry(String from, String to, String event, Map<String, Object> args);

}
