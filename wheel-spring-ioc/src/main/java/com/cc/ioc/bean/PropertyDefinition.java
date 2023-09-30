package com.cc.ioc.bean;

import lombok.Data;
import lombok.ToString;

/**
 * bean 构造期定义
 * @author cc
 * @date 2023/9/30
 */
@Data
@ToString
public class PropertyDefinition {

    private int index;
    private String ref;
    private String name;
    private Object value;
}
