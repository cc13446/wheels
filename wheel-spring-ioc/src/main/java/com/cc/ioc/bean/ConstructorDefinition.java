package com.cc.ioc.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * bean 构造方法定义
 * @author cc
 * @date 2023/9/30
 */
@Data
@ToString
public class ConstructorDefinition {

    List<BeanDefinition> beanDefinitions;
}
