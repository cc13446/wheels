package com.cc.ioc.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * bean 定义
 * @author cc
 * @date 2023/9/30
 */
@Getter
@AllArgsConstructor
@ToString
public class BeanDefinition {

    private final boolean primary;
    private final String name;
    private final Set<String> classNames;
    private final Set<String> interfaceNames;
    private final ConstructorDefinition constructorDefinition;
    private final List<PropertyDefinition> propertyDefinitions;

}
