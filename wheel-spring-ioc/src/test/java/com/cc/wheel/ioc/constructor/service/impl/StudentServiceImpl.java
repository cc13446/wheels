package com.cc.wheel.ioc.constructor.service.impl;

import com.cc.wheel.ioc.annotation.Component;
import com.cc.wheel.ioc.constructor.service.StudentService;

/**
 * @author cc
 * @date 2023/10/1
 */
@Component
public class StudentServiceImpl implements StudentService {

    public static final String STUDENT = "STUDENT";

    @Override
    public String student() {
        return STUDENT;
    }
}
