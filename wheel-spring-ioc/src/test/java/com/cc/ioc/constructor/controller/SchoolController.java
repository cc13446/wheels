package com.cc.ioc.constructor.controller;

import com.cc.ioc.annotation.Autowired;
import com.cc.ioc.annotation.Component;
import com.cc.ioc.annotation.Qualifier;
import com.cc.ioc.constructor.service.StudentService;
import com.cc.ioc.constructor.service.TeacherService;

/**
 * @author cc
 * @date 2023/10/1
 */
@Component
public class SchoolController {

    private final StudentService studentService;
    private final TeacherService teacherService;

    @Autowired
    public SchoolController(StudentService studentService, @Qualifier("Teacher") TeacherService teacherService) {
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    public String student() {
        return studentService.student();
    }

    public String teacher() {
        return teacherService.teacher();
    }
}
