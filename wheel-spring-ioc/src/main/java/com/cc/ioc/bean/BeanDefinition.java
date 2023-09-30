package com.cc.ioc.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * bean 定义
 * @author cc
 * @date 2023/9/30
 */
@Data
@ToString
public class BeanDefinition {

    private boolean primary;
    private String name;
    private String className;
    private List<String> interfaceNames;
    private ConstructorDefinition constructorDefinition;
    private List<PropertyDefinition> propertyDefinitions;

}
