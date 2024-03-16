package com.cc.wheel.loader.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 断言工具类
 * @author cc
 * @date 2023/8/19
 */
public class AssertUtils {

    public static <T> T assertNonNull(T object, String errMessage) {
        if (Objects.isNull(object)) {
            throw new RuntimeException(errMessage);
        }
        return object;
    }

    public static String assertNonBlank(String object, String errMessage) {
        if (StringUtils.isBlank(object)) {
            throw new RuntimeException(errMessage);
        }
        return object;
    }
}
