package com.cc.wheel;

import java.util.Properties;

/**
 * @author: cc
 * @date: 2023/8/27
 */
public class TestResourceWrapper extends ResourceWrapper<Integer> {

    public static final String KEY = "key";

    public TestResourceWrapper() {

    }

    public TestResourceWrapper(Properties properties) {
        super(properties);
        this.resource = Integer.parseInt(String.valueOf(properties.get(KEY)));
    }

    @Override
    public void close() {
        this.resource = null;
    }
}
