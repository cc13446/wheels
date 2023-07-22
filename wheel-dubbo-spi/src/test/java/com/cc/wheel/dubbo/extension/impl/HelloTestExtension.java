package com.cc.wheel.dubbo.extension.impl;

import com.cc.wheel.dubbo.common.URL;
import com.cc.wheel.dubbo.extension.TestExtension;

/**
 * @author cc
 * @date 2023/7/22
 */
public class HelloTestExtension implements TestExtension {
    @Override
    public String test(URL url) {
        return "hello";
    }
}
