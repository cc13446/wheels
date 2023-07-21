package com.cc.wheel.dubbo.extension.utils;

/**
 * @author cc
 * @date 2023/7/22
 */
public class ClassCastUtil {


    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}
