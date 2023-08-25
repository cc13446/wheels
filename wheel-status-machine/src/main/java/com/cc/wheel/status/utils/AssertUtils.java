package com.cc.wheel.status.utils;

import com.cc.wheel.status.exception.StatusMachineException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
/**
 * 断言工具类
 * @author cc
 * @date 2023/8/19
 */
public class AssertUtils {

    public static <T> void assertNonNull(T object, String errMessage) {
        if (Objects.isNull(object)) {
            throw new StatusMachineException(errMessage);
        }
    }

    public static void assertNonBlank(String object, String errMessage) {
        if (StringUtils.isBlank(object)) {
            throw new StatusMachineException(errMessage);
        }
    }

    public static void assertTrue(boolean object, String errMessage) {
        if (!object) {
            throw new StatusMachineException(errMessage);
        }
    }

}
