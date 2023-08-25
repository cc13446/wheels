package com.cc.wheel.status.domain;

/**
 * 错误状态处理，设置状态机状态错误时，调用此方法，并返回正确的状态
 * @author cc
 * @date 2023/8/19
 */
@FunctionalInterface
public interface ErrorStatusHandler {

    /**
     * 设置状态机状态错误时，调用此方法，并返回正确的状态
     * @param errorStatus 错误状态
     * @return 应该调整到的正确状态，或者可以抛出异常
     */
    String onErrorStatus(String errorStatus);

}
