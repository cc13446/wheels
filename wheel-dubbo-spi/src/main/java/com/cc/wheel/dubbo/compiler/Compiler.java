package com.cc.wheel.dubbo.compiler;

import com.cc.wheel.dubbo.extension.SPI;

/**
 * Compiler. (SPI, Singleton, ThreadSafe)
 * @author cc
 * @date 2023/7/22
 */
@SPI("javassist")
public interface Compiler {

    /**
     * Compile java source code.
     *
     * @param code        Java source code
     * @param classLoader classloader
     * @return Compiled class
     */
    Class<?> compile(String code, ClassLoader classLoader);

}
