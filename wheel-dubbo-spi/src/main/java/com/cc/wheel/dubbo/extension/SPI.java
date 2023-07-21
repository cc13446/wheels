package com.cc.wheel.dubbo.extension;

import java.lang.annotation.*;

/**
 * 此注解标注的接口是被SPI扩展管理器管理 <br>
 * 属性 value 代表默认的扩展name <br>
 * 如果用户在使用扩展的时候没有指定name，则使用默认实现 <br>
 * 扩展配置文件的例子如下：<br>
 * <pre>
 * extension name = class name
 *
 * xxx = com.foo.XxxProtocol
 * yyy = com.foo.YyyProtocol
 * </pre>
 *
 * @author cc
 * @date 2023/7/22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * default extension name
     */
    String value() default "";
}
