package com.cc.wheel.factory;

import lombok.Getter;

import java.util.Properties;

/**
 * 资源描述
 * @author: cc
 * @date: 2023/8/27
 */
@Getter
public abstract class ResourceWrapper<T> implements AutoCloseable {

    /**
     * 资源
     */
    protected T resource;

    public ResourceWrapper() {

    }

    public ResourceWrapper(Properties properties) {

    }

    /**
     * 关闭相应的资源
     */
    @Override
    public abstract void close();

}
