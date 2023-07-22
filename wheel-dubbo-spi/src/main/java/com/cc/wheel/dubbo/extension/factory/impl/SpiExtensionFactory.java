package com.cc.wheel.dubbo.extension.factory.impl;


import com.cc.wheel.dubbo.extension.ExtensionLoader;
import com.cc.wheel.dubbo.extension.SPI;
import com.cc.wheel.dubbo.extension.factory.ExtensionFactory;

/**
 * SpiExtensionFactory
 */
public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            if (!loader.getSupportedExtensions().isEmpty()) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }

}
