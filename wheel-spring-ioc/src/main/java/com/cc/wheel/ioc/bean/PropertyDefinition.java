package com.cc.wheel.ioc.bean;

import com.cc.wheel.ioc.bean.base.NameDefinition;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * bean 构造期定义
 * @author cc
 * @date 2023/9/30
 */
@Getter
@ToString
public class PropertyDefinition extends NameDefinition {
    private final Method method;

    public PropertyDefinition(String name, Set<String> classNames, Set<String> interfaceNames, Method method) {
        super(name, classNames, interfaceNames);
        this.method = method;
    }
}
