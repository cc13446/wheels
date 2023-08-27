package com.cc.wheel;


/**
 * 资源工厂
 * @author: cc
 * @date: 2023/8/27
 */
public interface ResourceFactory<T> {

    /**
     * @return 资源
     */
    ResourceWrapper<T> getResource();


    void shutdown();
}
