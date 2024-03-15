package com.cc.wheel.loader.api;

import java.util.Map;

/**
 * bundle 接口
 * @author cc
 * @date 2024/3/15
 */
public interface Bundle {

    /**
     * 装载模块
     */
    void start();

    /**
     * 卸载模块
     */
    void stop();

    /**
     * 触发模块事件
     * @param param 参数
     */
    void event(Map<String, String> param);
}
