package com.cc.wheel.ioc.property.service.impl;

import com.cc.wheel.ioc.annotation.Autowired;
import com.cc.wheel.ioc.annotation.Component;
import com.cc.wheel.ioc.annotation.Qualifier;
import com.cc.wheel.ioc.property.service.EmailService;
import com.cc.wheel.ioc.property.service.UserService;

/**
 * @author cc
 * @date 2023/10/1
 */

@Component("Email")
public class EmailServiceImpl implements EmailService {
    public static String EMAIL = "EMAIL";

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
    public String email() {
        return EMAIL;
    }

    @Override
    public String hello() {
        return userService.hello();
    }

    @Override
    public String selfEmail() {
        return emailService.email();
    }
}
