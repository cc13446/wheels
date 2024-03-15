package com.cc.wheel.loader.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 事件处理器
 *
 * @author cc
 * @date 2024/3/15
 */
@Slf4j
public class EventHandle {

    /**
     * 时间处理
     */
    public void event(Map<String, String> map) {
        log.info("Demo event, param = {}", map);
    }

}
