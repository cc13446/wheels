package com.cc.wheel.dubbo.extension;

import java.lang.annotation.*;

/**
 * 可以标注在两个地方：<br>
 * 1. 类：编码实现的自适应类 <br>
 * 2. 方法：生成代码对方法进行自适应扩展 <br>
 *
 * 自适应扩展指的是，按照方法传入的URL获取扩展的名字，然后将实现交给对应扩展的相应方法
 *
 * @author cc
 * @date 2023/7/22
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {


    /**
     * URL中扩展名字对应的key列表，自适应方法会按照数组顺序的优先级从URL中获取扩展的名字 <br>
     * 如果都获取不到，则使用{@link SPI}注解的默认名字 <br>
     * 如果没有默认名字，则抛出{@link IllegalStateException}
     *
     * @see com.cc.wheel.dubbo.common.URL
     */
    String[] value() default {};
}
