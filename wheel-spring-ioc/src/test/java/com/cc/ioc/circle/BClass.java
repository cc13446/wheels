package com.cc.ioc.circle;

import com.cc.ioc.annotation.Autowired;
import com.cc.ioc.annotation.Component;

/**
 * @author cc
 * @date 2023/10/1
 */

@Component
public class BClass {

    private final AClass aClass;

    @Autowired
    public BClass(AClass aClass) {
        this.aClass = aClass;
    }
}
