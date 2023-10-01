package com.cc.ioc.circle;

import com.cc.ioc.annotation.Autowired;
import com.cc.ioc.annotation.Component;

/**
 * @author cc
 * @date 2023/10/1
 */

@Component
public class AClass {

    private final BClass bClass;

    @Autowired
    public AClass(BClass bClass) {
        this.bClass = bClass;
    }

    public String a() {
        return "a";
    }
}
