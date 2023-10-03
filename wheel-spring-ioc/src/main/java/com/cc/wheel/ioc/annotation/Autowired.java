package com.cc.wheel.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author cc
 * @date 2023/9/30
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

}
