package com.cc.wheel.loader;

import com.cc.wheel.loader.api.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * bundle 管理器
 *
 * @author cc
 * @date 2024/3/16
 */
public class BundleManager {

    private final Map<String, Bundle> bundleMap = new HashMap<>();

    private final Map<String, BundleClassLoader> loaderMap = new HashMap<>();

    /**
     * 加载模块
     *
     * @param path 文件路径
     * @param id   模块id
     */
    public synchronized void loadBundle(String path, String id) {
        if (bundleMap.containsKey(id)) {
            throw new RuntimeException("Bundle has existed " + id);
        }
        BundleClassLoader classLoader = new BundleClassLoader(this.getClass().getClassLoader(), id + "_loader", path);
        Bundle bundle = classLoader.getBundle();
        bundleMap.put(id, bundle);
        loaderMap.put(id, classLoader);
        bundle.start();
    }

    /**
     * 卸载模块
     *
     * @param id 模块id
     */
    public synchronized void destroyBundle(String id) {
        loaderMap.remove(id);
        bundleMap.remove(id).stop();
    }

    /**
     * 获取模块
     *
     * @param id 模块id
     */
    public synchronized Bundle getBundle(String id) {
        return bundleMap.getOrDefault(id, null);
    }

    /**
     * 获取模块类加载器
     *
     * @param id 模块id
     */
    public synchronized BundleClassLoader getLoader(String id) {
        return loaderMap.getOrDefault(id, null);
    }
}
