package com.cc.ioc.bean;

import com.cc.ioc.bean.base.NameDefinition;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * bean 构造方法定义
 * @author cc
 * @date 2023/9/30
 */
@Getter
@ToString
public class ConstructorDefinition extends NameDefinition {

    private final Class<?> type;

    public ConstructorDefinition(String name, Set<String> classNames, Set<String> interfaceNames, Class<?> type) {
        super(name, classNames, interfaceNames);
        this.type = type;
    }
}
