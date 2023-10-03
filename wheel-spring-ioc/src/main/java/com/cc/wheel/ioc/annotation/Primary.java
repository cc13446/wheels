package com.cc.wheel.ioc.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a bean should be given preference when multiple candidates
 * are qualified to autowire a single-valued dependency. If exactly one
 * 'primary' bean exists among the candidates, it will be the autowired value.
 *
 * @author cc
 * @date 2023/9/30
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {

}
