package com.cc.wheel.loader;

import lombok.Data;

/**
 * bundle 配置
 * @author cc
 * @date 2024/3/16
 */
@Data
public class BundleProperty {

    static final String ISOLATED_PREFIX = "isolated_prefix";

    static final String BUNDLE_IMPL = "bundle_impl";

    private String prefix;

    private String bundleImpl;

}
