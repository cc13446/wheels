package com.cc.wheel.ioc.bean;

import com.cc.wheel.ioc.bean.base.NameDefinition;
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
public class BeanDefinition extends NameDefinition {

    private final boolean primary;
    private final List<ConstructorDefinition> constructorDefinitions;
    private final List<PropertyDefinition> propertyDefinitions;
    private final Class<?> type;

    @Setter
    private volatile Object value;
    @Setter
    private volatile Object preValue;
    @Setter
    private volatile boolean creating;

    public BeanDefinition(boolean primary,
                          String name,
                          Set<String> classNames,
                          Set<String> interfaceNames,
                          List<ConstructorDefinition> constructorDefinitions,
                          List<PropertyDefinition> propertyDefinitions,
                          Class<?> type) {
        super(name, classNames, interfaceNames);
        this.primary = primary;
        this.constructorDefinitions = constructorDefinitions;
        this.propertyDefinitions = propertyDefinitions;
        this.type = type;
    }

}
