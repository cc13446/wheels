package com.cc.wheel.ioc.property.controller;

import com.cc.wheel.ioc.annotation.Autowired;
import com.cc.wheel.ioc.annotation.Component;
import com.cc.wheel.ioc.annotation.Qualifier;
import com.cc.wheel.ioc.property.service.EmailService;
import com.cc.wheel.ioc.property.service.UserService;

/**
 * @author cc
 * @date 2023/10/1
 */
@Component
public class UserController {

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

    public String hello() {
        return this.userService.hello();
    }

    public String email() {
        return this.emailService.email();
    }

    public String selfHello() {
        return this.userService.selfHello();
    }

    public String selfEmail() {
        return this.emailService.selfEmail();
    }

    public String emailHello() {
        return this.emailService.hello();
    }

    public String helloEmail() {
        return this.userService.email();
    }

}
