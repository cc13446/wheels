package com.cc.wheel.status.domain;

import com.cc.wheel.status.utils.AssertUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author cc
 * @date 2023/8/19
 */
public class StatusMachine {

    private volatile Status curStatus;

    private final ErrorStatusHandler errorStatusHandler;

    private final Map<String, Status> statusMap;

    private final Map<String, Event> eventMap;

    public StatusMachine(String curStatus, ErrorStatusHandler errorStatusHandler, Map<String, Status> statusMap, Map<String, Event> eventMap) {
        this.errorStatusHandler = errorStatusHandler;
        this.statusMap = Collections.unmodifiableMap(statusMap);
        this.eventMap = Collections.unmodifiableMap(eventMap);
        setCurStatus(curStatus, null, null);
    }

    /** 设置状态
     * @param statusName 状态名字
     * @param eventName 事件名字，如果是设置而不是外部触发的话则为null
     */
    private void setCurStatus(String statusName, String eventName, Map<String, Object> args) {
        Status status = statusMap.get(statusName);
        if (Objects.isNull(status)) {
            statusName = errorStatusHandler.onErrorStatus(statusName);
            AssertUtils.assertNonBlank(statusName, "状态错误处理返回的正确状态不能为空！");
            status = statusMap.get(statusName);
            AssertUtils.assertNonNull(status, "状态错误处理返回的正确状态不合法！");
        }
        // 状态机状态转移触发的
        if (StringUtils.isNotBlank(eventName) && Objects.nonNull(this.curStatus)) {
            String from = this.curStatus.getStatusName();
            this.curStatus = status;
            String nextEvent = this.curStatus.callEntry(from, statusName, eventName, args);
            if (StringUtils.isNotBlank(nextEvent)) {
                this.fire(nextEvent, args);
            }
        } else {
            // 状态机初始化触发的
            this.curStatus = status;
        }
    }

    public synchronized StatusMachine fire(String eventName, Map<String, Object> args) {
        AssertUtils.assertNonBlank(eventName, "事件名字不能为空！");
        Event event = eventMap.get(eventName);
        AssertUtils.assertNonNull(event, "事件[" + eventName + "]不合法！");
        String from = curStatus.getStatusName();
        String to = curStatus.getNextStatusName(eventName);

        // 触发出口
        this.curStatus.callExit(from, to, eventName, args);
        // 触发守卫
        if (!event.onGuard(from, to, eventName, args)) {
            return this ;
        }
        // 触发转移回调
        if (!event.onCall(from, to, eventName, args) && event.isTransaction()) {
            return this;
        }
        // 转移状态，触发入口回调
        this.setCurStatus(to, eventName, args);

        return this;
    }

    public synchronized String curStatus() {
        return this.curStatus.getStatusName();
    }
}
