package com.cc.wheel.loader.demo;

import com.cc.wheel.loader.api.Bundle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author cc
 * @date 2024/3/15
 */
@Slf4j
public class BundleImpl implements Bundle {

    private final EventHandle handle = new EventHandle();

    @Override
    public void start() {
        log.info("Demo start");
    }

    @Override
    public void stop() {
        log.info("Demo stop");
    }

    @Override
    public void event(Map<String, String> param) {
        handle.event(param);
    }
}
