package com.cc.ioc.constructor.service.impl;

import com.cc.ioc.annotation.Component;
import com.cc.ioc.constructor.service.StudentService;

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
