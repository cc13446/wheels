package com.cc.wheel.dubbo.extension.factory;

import com.cc.wheel.dubbo.extension.SPI;

/**
 * 扩展工厂的接口，根据扩展的类型和名字获取扩展
 * @author cc
 * @date 2023/7/22
 */

@SPI
public interface ExtensionFactory {

    /**
     * @param type 扩展类型
     * @param name 扩展名字
     * @param <T> 扩展类
     * @return 扩展
     */
    <T> T getExtension(Class<T> type, String name);
}
