package com.cc.ioc.bean;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * bean 构造期定义
 * @author cc
 * @date 2023/9/30
 */
@Data
@ToString
public class PropertyDefinition {
    private final Method method;
    private final String name;
    private final Set<String> classNames;
    private final Set<String> interfaceNames;
}
