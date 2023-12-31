package com.cc.wheel.ioc.property.service.impl;

import com.cc.wheel.ioc.annotation.Autowired;
import com.cc.wheel.ioc.annotation.Component;
import com.cc.wheel.ioc.annotation.Qualifier;
import com.cc.wheel.ioc.property.service.EmailService;
import com.cc.wheel.ioc.property.service.UserService;

/**
 * @author cc
 * @date 2023/9/30
 */
@Component
public class UserServiceImpl implements UserService {

    public static String HELLO = "HELLO";

    private UserService userService;

    private EmailService emailService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Qualifier("Email")
    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public String hello() {
        return HELLO;
    }

    @Override
    public String email() {
        return emailService.email();
    }

    @Override
    public String selfHello() {
        return userService.hello();
    }
}
