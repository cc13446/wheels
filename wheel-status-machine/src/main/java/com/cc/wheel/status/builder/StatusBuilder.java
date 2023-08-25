package com.cc.wheel.status.builder;

import com.cc.wheel.status.domain.*;
import com.cc.wheel.status.exception.StatusMachineException;
import com.cc.wheel.status.utils.AssertUtils;
import com.cc.wheel.status.utils.ConcurrentHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cc
 * @date 2023/8/19
 */

public class StatusBuilder {

    private final StatusMachineBuilder statusMachineBuilder;

    private final String statusName;

    private final Set<EntryCall> entryCallSet = new ConcurrentHashSet<>();

    private final Set<ExitCall> exitCallSet = new ConcurrentHashSet<>();

    private final Map<String, String> eventNextStatusMap = new ConcurrentHashMap<>();

    public StatusBuilder(StatusMachineBuilder statusMachineBuilder, String statusName) {
        AssertUtils.assertNonBlank(statusName, "状态名字为空!");
        this.statusMachineBuilder = statusMachineBuilder;
        this.statusName = statusName;
    }

    /**
     * @return 状态名字
     */
    public String getStatusName() {
        return statusName;
    }

    /**
     * @param entryCall 入口回调
     * @return 状态构造器
     */
    public StatusBuilder addEntryCall(EntryCall entryCall) {
        entryCallSet.add(entryCall);
        return this;
    }

    /**
     * @param entryCall 入口回调
     * @return 状态构造器
     */
    public StatusBuilder removeEntryCall(EntryCall entryCall) {
        entryCallSet.remove(entryCall);
        return this;
    }

    /**
     * @param entryCall 入口回调
     * @return 状态构造器
     */
    public StatusBuilder addEntryCall(Set<EntryCall> entryCall) {
        entryCallSet.addAll(entryCall);
        return this;
    }


    /**
     * @param exitCall 出口回调
     * @return 状态构造器
     */
    public StatusBuilder addExitCall(Set<ExitCall> exitCall) {
        exitCallSet.addAll(exitCall);
        return this;
    }

    /**
     * @param exitCall 出口回调
     * @return 状态构造器
     */
    public StatusBuilder removeExitCall(ExitCall exitCall) {
        exitCallSet.remove(exitCall);
        return this;
    }

    /**
     * @param exitCall 出口回调
     * @return 状态构造器
     */
    public StatusBuilder addExitCall(ExitCall exitCall) {
        exitCallSet.add(exitCall);
        return this;
    }

    /**
     * @param eventName 事件名字
     * @param nextStatusName 事件转移
     * @param eventCalls 事件转移回调集合
     * @param eventGuards 事件守卫集合
     * @return 状态构造器
     */
    public StatusBuilder permit(String eventName, String nextStatusName, Set<EventCall> eventCalls, Set<EventGuard> eventGuards, boolean isTransaction) {
        if (Objects.nonNull(eventNextStatusMap.get(eventName)) && !nextStatusName.equals(eventNextStatusMap.get(eventName))) {
            throw new StatusMachineException("多次配置事件，并转移到不同的状态！");
        }
        EventBuilder eventBuilder = statusMachineBuilder.configEvent(eventName)
                .addEventCall(eventCalls)
                .addEventGuard(eventGuards)
                .configTransaction(isTransaction);
        StatusBuilder nextStatusBuilder = statusMachineBuilder.configStatus(nextStatusName);
        eventNextStatusMap.put(eventBuilder.getEventName(), nextStatusBuilder.getStatusName());
        return this;
    }

    /**
     * @param eventName 事件名字
     * @param nextStatusName 事件转移
     * @param eventCall 事件转移回调
     * @param eventGuard 事件守卫
     * @return 状态构造器
     */
    public StatusBuilder permit(String eventName, String nextStatusName, EventCall eventCall, EventGuard eventGuard) {
        return permit(eventName, nextStatusName, Set.of(eventCall), Set.of(eventGuard), true);
    }

    /**
     * @param eventName 事件名字
     * @param nextStatusName 事件转移
     * @return 状态构造器
     */
    public StatusBuilder permit(String eventName, String nextStatusName) {
        return permit(eventName, nextStatusName, new HashSet<>(), new HashSet<>(), true);
    }

    /**
     * @return 只读的 eventNextStatusMap
     */
    Map<String, String> getEventNextStatusMap() {
        return Collections.unmodifiableMap(this.eventNextStatusMap);
    }

    Status build() {
       return new Status(statusName, entryCallSet, exitCallSet, eventNextStatusMap);
    }


}
