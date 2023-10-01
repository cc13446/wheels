package com.cc.ioc.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * bean 定义
 * @author cc
 * @date 2023/9/30
 */
@Getter
@ToString
public class BeanDefinition {

    private final boolean primary;
    private final String name;
    private final Set<String> classNames;
    private final Set<String> interfaceNames;
    private final List<ConstructorDefinition> constructorDefinitions;
    private final List<PropertyDefinition> propertyDefinitions;
    private final Class<?> type;

    @Setter
    private volatile Object value;
    @Setter
    private volatile Object preValue;
    @Setter
    private volatile boolean processing;

    public BeanDefinition(boolean primary,
                          String name,
                          Set<String> classNames,
                          Set<String> interfaceNames,
                          List<ConstructorDefinition> constructorDefinitions,
                          List<PropertyDefinition> propertyDefinitions,
                          Class<?> type) {
        this.primary = primary;
        this.name = name;
        this.classNames = classNames;
        this.interfaceNames = interfaceNames;
        this.constructorDefinitions = constructorDefinitions;
        this.propertyDefinitions = propertyDefinitions;
        this.type = type;
    }

}
