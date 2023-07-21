package com.cc.wheel.dubbo.extension;

import com.cc.wheel.dubbo.extension.utils.ClassCastUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * SPI扩展的核心类：扩展加载器，主要有以下功能：<br>
 * 1. 通过静态Map管理不同类的扩展加载器 <br>
 * 2.
 * @author cc
 * @date 2023/7/21
 */
public class ExtensionLoader<T> {

    // 静态属性和方法

    /**
     * 静态Map，存储了每个类对应的扩展集加载器
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);

    /**
     *
     * @param type 扩展类型
     * @param <T> 扩展类型
     * @return 此类型的扩展加载器
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null!");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("Extension type(" + type + ") is without @" + SPI.class.getSimpleName() + " Annotation!");
        }

        // 先从缓存里面拿
        ExtensionLoader<T> loader = ClassCastUtil.cast(EXTENSION_LOADERS.get(type));
        if (loader == null) {
            // 拿不到，建一个
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = ClassCastUtil.cast(EXTENSION_LOADERS.get(type));
        }
        return loader;
    }


    // 普通属性和方法

    /**
     * 扩展加载器管理的扩展类类型
     */
    private final Class<T> type;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

}
