package com.cc.wheel.status.domain;

import com.cc.wheel.status.utils.AssertUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cc
 * @date 2023/8/19
 */
public class Status {

    private final String statusName;

    private final List<EntryCall> entryCallList;

    private final List<ExitCall> exitCallList;

    private final Map<String, String> eventNextStatusMap;

    public Status(String statusName, Set<EntryCall> entryCallSet, Set<ExitCall> exitCallSet, Map<String, String> eventNextStatusMap) {
        this.statusName = statusName;
        this.entryCallList = entryCallSet.stream().sorted(Comparator.comparingInt(RankCall::rank)).collect(Collectors.toUnmodifiableList());
        this.exitCallList = exitCallSet.stream().sorted(Comparator.comparingInt(RankCall::rank)).collect(Collectors.toUnmodifiableList());
        this.eventNextStatusMap = Collections.unmodifiableMap(eventNextStatusMap);
    }

    /**
     * @return 状态名字
     */
    public String getStatusName() {
        return statusName;
    }

    /**
     * @param eventName 事件名
     * @return 下一个状态名
     */
    String getNextStatusName(String eventName) {
        AssertUtils.assertNonBlank(eventName, "事件名字不能为空！");
        String nextStatusName = eventNextStatusMap.get(eventName);
        AssertUtils.assertNonBlank(nextStatusName, "下一个状态不合法！");
        return nextStatusName;
    }

    /**
     * 触发入口回调
     *
     * @param from  从哪个状态来
     * @param to    往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     * @return 入口自动触发的事件，没有则返回null
     */
    String callEntry(String from, String to, String event, Map<String, Object> args) {
        String nextEvent = null;
        for (EntryCall entryCall : entryCallList) {
            nextEvent = entryCall.entry(from, to, event, args);
        }
        return nextEvent;
    }

    /**
     * 触发出口回调
     *
     * @param from  从哪个状态来
     * @param to    往那个状态去
     * @param event 发生了哪个事件
     * @param args  参数
     */
    void callExit(String from, String to, String event, Map<String, Object> args) {
        for (ExitCall e : exitCallList) {
            e.exit(from, to, event, args);
        }
    }
}
