package com.cc.wheel.ioc.bean.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * @author cc
 * @date 2023/10/1
 */
@Getter
@AllArgsConstructor
@ToString
public class NameDefinition {
    protected final String name;
    protected final Set<String> classNames;
    protected final Set<String> interfaceNames;
}
