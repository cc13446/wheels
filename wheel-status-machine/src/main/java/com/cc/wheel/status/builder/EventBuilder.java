package com.cc.wheel.status.builder;

import com.cc.wheel.status.domain.Event;
import com.cc.wheel.status.domain.EventCall;
import com.cc.wheel.status.domain.EventGuard;
import com.cc.wheel.status.utils.AssertUtils;
import com.cc.wheel.status.utils.ConcurrentHashSet;

import java.util.Set;

/**
 * @author cc
 * @date 2023/8/19
 */
public class EventBuilder {

    private final StatusMachineBuilder statusMachineBuilder;

    private final String eventName;

    private boolean isTransaction;

    private final Set<EventCall> eventCallSet = new ConcurrentHashSet<>();

    private final Set<EventGuard> eventGuardSet = new ConcurrentHashSet<>();

    public EventBuilder(StatusMachineBuilder statusMachineBuilder, String eventName) {
        AssertUtils.assertNonBlank(eventName, "事件名字为空!");
        this.statusMachineBuilder = statusMachineBuilder;
        this.eventName = eventName;
    }

    /**
     * @return 事件名字
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @param isTransactionCall 是否事务
     * @return 事件构造器
     */
    public EventBuilder configTransaction(boolean isTransactionCall) {
        this.isTransaction = isTransactionCall;
        return this;
    }

    /**
     * @param eventCall 事件回调
     * @return 事件构造器
     */
    public EventBuilder addEventCall(EventCall eventCall) {
        AssertUtils.assertNonNull(eventCall, "事件回调为空!");
        eventCallSet.add(eventCall);
        return this;
    }

    /**
     * @param eventCall 事件回调
     * @return 事件构造器
     */
    public EventBuilder addEventCall(Set<EventCall> eventCall) {
        AssertUtils.assertNonNull(eventCall, "事件回调为空!");
        eventCall.forEach(e -> AssertUtils.assertNonNull(e, "事件回调为空!"));
        eventCallSet.addAll(eventCall);
        return this;
    }


    /**
     * @param eventGuard 事件守卫
     * @return 事件构造器
     */
    public EventBuilder addEventGuard(EventGuard eventGuard) {
        AssertUtils.assertNonNull(eventGuard, "事件守卫为空!");
        eventGuardSet.add(eventGuard);
        return this;
    }

    /**
     * @param eventGuard 事件守卫
     * @return 事件构造器
     */
    public EventBuilder addEventGuard(Set<EventGuard> eventGuard) {
        AssertUtils.assertNonNull(eventGuard, "事件守卫为空!");
        eventGuard.forEach(e -> AssertUtils.assertNonNull(e, "事件守卫为空!"));
        eventGuardSet.addAll(eventGuard);
        return this;
    }


    Event build() {
        return new Event(this.eventName, this.eventCallSet, this.eventGuardSet, this.isTransaction);
    }

}
