package com.cc.wheel.dubbo.utils;

/**
 * @author cc
 * @date 2023/7/22
 */
public class ClassCastUtil {


    /**
     * 为了避免消除警告的注解满天飞，在这里统一处理强制类型转换
     * @param o 要转换的对象
     * @param <T> 类型
     * @return 强制转换的结果
     * @throws ClassCastException 类型转换失败错误
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) throws ClassCastException {
        return (T) o;
    }
}
