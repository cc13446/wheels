package com.cc.wheel.dubbo.extension;

import com.cc.wheel.dubbo.common.URL;

/**
 * @author cc
 * @date 2023/7/22
 */
@SPI("hello")
public interface TestExtension {

    @Adaptive("key")
    String test(URL url);
}
