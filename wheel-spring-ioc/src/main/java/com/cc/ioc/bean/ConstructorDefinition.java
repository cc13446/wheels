package com.cc.ioc.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * bean 构造方法定义
 * @author cc
 * @date 2023/9/30
 */
@AllArgsConstructor
@Getter
@ToString
public class ConstructorDefinition {
    private final String name;
    private final Set<String> classNames;
    private final Set<String> interfaceNames;
    private final Class<?> type;
}
