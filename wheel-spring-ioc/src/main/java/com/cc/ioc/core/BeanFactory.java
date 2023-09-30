package com.cc.ioc.core;

/**
 * bean工厂接口
 * @author cc
 * @date 2023/9/30
 */
public interface BeanFactory {

    /**
     * @param name Bean Name
     * @param tClass Bean Class
     * @param <T> Bean Class
     * @return The Instance Of Bean
     */
    <T> T getBean(String name, Class<T> tClass);

    /**
     * @param tClass Bean Class
     * @param <T> Bean Class
     * @return The Instance Of Bean
     */
    <T> T getBean(Class<T> tClass);

}
