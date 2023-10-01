package com.cc.ioc.constructor.service.impl;

import com.cc.ioc.annotation.Component;
import com.cc.ioc.constructor.service.TeacherService;

/**
 * @author cc
 * @date 2023/10/1
 */
@Component("Teacher")
public class TeacherServiceImpl implements TeacherService {

    public static final String TEACHER = "TEACHER";

    @Override
    public String teacher() {
        return TEACHER;
    }
}
