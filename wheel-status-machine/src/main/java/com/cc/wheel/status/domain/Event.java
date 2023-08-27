package com.cc.wheel.status.domain;

import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cc
 * @date 2023/8/19
 */
public class Event {

    @Getter
    private final String eventName;

    private final boolean isTransaction;

    private final List<EventCall> eventCallList;

    private final List<EventGuard> eventGuardList;

    public Event(String eventName, Set<EventCall> eventCallSet, Set<EventGuard> eventGuardSet, boolean isTransaction) {
        this.eventName = eventName;
        this.eventCallList = eventCallSet.stream().sorted(Comparator.comparingInt(RankCall::rank)).collect(Collectors.toUnmodifiableList());
        this.eventGuardList = eventGuardSet.stream().sorted(Comparator.comparingInt(RankCall::rank)).collect(Collectors.toUnmodifiableList());
        this.isTransaction = isTransaction;
    }

    /**
     * 如果true，转移回调执行失败或者报错则不进行状态转移
     * 如果false，忽略回调执行失败
     */
    public boolean isTransaction() {
        return isTransaction;
    }

    /**
     * 触发转移守卫
     *
     * @param from  从哪个状态来
     * @param to    往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     * @return 回调是否转移成功
     */
    boolean onGuard(String from, String to, String event, Map<String, Object> args) {
        boolean res = true;
        for (EventGuard guard : this.eventGuardList) {
            res = res && guard.tryTrans(from, to, event, args);
        }
        return res;
    }

    /**
     * 触发转移回调
     *
     * @param from  从哪个状态来
     * @param to    往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     * @return 回调是否转移成功
     */
    boolean onCall(String from, String to, String event, Map<String, Object> args) {
        boolean res = true;
        for (EventCall call : this.eventCallList) {
            res = res && call.trans(from, to, event, args);
        }
        return res;
    }
}
