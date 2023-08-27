package com.cc.wheel.unpool;

import com.cc.wheel.ResourceWrapper;
import com.cc.wheel.ResourceFactory;

import java.util.Properties;

/**
 * 未池化的资源工厂
 *
 * @author: cc
 * @date: 2023/8/27
 */
public class UnPoolResourceFactory<T> implements ResourceFactory<T> {


    private final Properties properties;

    private final Class<? extends ResourceWrapper<T>> tClass;

    public UnPoolResourceFactory(Properties properties, Class<? extends ResourceWrapper<T>> tClass) {
        this.properties = properties;
        this.tClass = tClass;
    }

    @Override
    public ResourceWrapper<T> getResource() {
        try {
            return tClass.getDeclaredConstructor(Properties.class).newInstance(properties);
        } catch (Exception e) {
            throw new RuntimeException("Create new resource wrapper with properties fail", e);
        }
    }

    @Override
    public void shutdown() {

    }
}
