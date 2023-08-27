package com.cc.wheel.status.builder;

import com.cc.wheel.status.domain.ErrorStatusHandler;
import com.cc.wheel.status.domain.Event;
import com.cc.wheel.status.domain.Status;
import com.cc.wheel.status.domain.StatusMachine;
import com.cc.wheel.status.exception.StatusMachineException;
import com.cc.wheel.status.utils.AssertUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cc
 * @date 2023/8/19
 */
public class StatusMachineBuilder {

    private String defaultInitStatus = null;

    private ErrorStatusHandler errorStatusHandler = errStatus -> {
        throw new StatusMachineException("状态机运行错误，状态[" + errStatus + "]不合法！");
    };

    private final Map<String, StatusBuilder> statusMap = new ConcurrentHashMap<>();
    private final Map<String, EventBuilder> eventMap = new ConcurrentHashMap<>();

    /**
     * 配置初始状态
     * @param statusName 状态名字
     * @return 状态建造者
     */
    public StatusBuilder configInitStatus(String statusName) {
        StatusBuilder statusBuilder = configStatus(statusName);
        this.defaultInitStatus = statusName;
        return statusBuilder;
    }

    /**
     * 配置错误状态处理
     * @param errorStatusHandler 错误处理
     * @return 状态机构建者
     */
    public StatusMachineBuilder configErrorStatusHandler(ErrorStatusHandler errorStatusHandler) {
        AssertUtils.assertNonNull(errorStatusHandler, "错误处理函数为空！");
        this.errorStatusHandler = errorStatusHandler;
        return this;
    }


    /**
     * 配置状态
     * @param statusName 状态名字
     * @return 状态建造者
     */
    public StatusBuilder configStatus(String statusName) {
        AssertUtils.assertNonBlank(statusName, "状态名字为空!");
        return statusMap.computeIfAbsent(statusName, (name) -> new StatusBuilder(this, name));
    }

    /**
     * 配置事件
     * @param eventName 事件名字
     * @return 事件构造器
     */
    public EventBuilder configEvent(String eventName) {
        AssertUtils.assertNonBlank(eventName, "事件名字为空!");
        return eventMap.computeIfAbsent(eventName, EventBuilder::new);
    }

    /**
     * @param eventName 事件名字
     * @return 是否有事件
     */
    public boolean hasEvent(String eventName) {
        return eventMap.containsKey(eventName);
    }


    /**
     * @param statusName 状态名字
     * @return 是否有状态
     */
    public boolean hasStatus(String statusName) {
        return statusMap.containsKey(statusName);
    }

    /**
     * 检测配置是否合法
     */
    public void validConfig() {
        AssertUtils.assertNonNull(this.defaultInitStatus, "默认初始状态不能为空！");
        AssertUtils.assertNonNull(this.errorStatusHandler, "错误处理函数为空！");
        AssertUtils.assertTrue(this.statusMap.containsKey(this.defaultInitStatus), "初始状态必须包含在状态集合中！");
        for (StatusBuilder statusBuilder : statusMap.values()) {
            for (Map.Entry<String, String> eventNext : statusBuilder.getEventNextStatusMap().entrySet()) {
                AssertUtils.assertTrue(this.eventMap.containsKey(eventNext.getKey()), "事件必须包含在事件集合中！");
                AssertUtils.assertTrue(this.statusMap.containsKey(eventNext.getValue()), "下一个状态必须包含在状态集合中！");
            }
        }
    }

    /**
     * @return 生成的状态机模型
     */
    public StatusMachine build(String initStatus) {
        if (Objects.isNull(initStatus)) {
            initStatus = this.defaultInitStatus;
        }
        this.validConfig();
        Map<String, Status> status = this.statusMap.values().stream().map(StatusBuilder::build).collect(Collectors.toMap(Status::getStatusName, s -> s));
        Map<String, Event> events = this.eventMap.values().stream().map(EventBuilder::build).collect(Collectors.toMap(Event::getEventName, s -> s));
        return new StatusMachine(initStatus, this.errorStatusHandler, status, events);
    }

    /**
     * @return 生成的状态机模型
     */
    public StatusMachine build() {
        return build(this.defaultInitStatus);
    }

}
