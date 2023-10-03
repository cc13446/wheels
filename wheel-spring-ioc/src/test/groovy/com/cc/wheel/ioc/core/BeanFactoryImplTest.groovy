package com.cc.wheel.ioc.core


import com.cc.wheel.ioc.circle.AClass
import com.cc.wheel.ioc.constructor.controller.SchoolController
import com.cc.wheel.ioc.constructor.service.impl.StudentServiceImpl
import com.cc.wheel.ioc.constructor.service.impl.TeacherServiceImpl
import com.cc.wheel.ioc.core.impl.BeanFactoryImpl
import com.cc.wheel.ioc.property.controller.UserController
import com.cc.wheel.ioc.property.service.impl.EmailServiceImpl
import com.cc.wheel.ioc.property.service.impl.UserServiceImpl
import spock.lang.Specification


/**
 * @author cc
 * @date 2023/10/1
 */
class BeanFactoryImplTest extends Specification {

    def "property di test"() {
        given:
        def packageName = "com.cc.wheel.ioc.property"
        BeanFactory beanFactory = new BeanFactoryImpl([packageName])

        when:
        UserController userController = beanFactory.getBean(UserController)

        then:
        userController.hello() == UserServiceImpl.HELLO
        userController.email() == EmailServiceImpl.EMAIL
        userController.selfHello() == UserServiceImpl.HELLO
        userController.selfEmail() == EmailServiceImpl.EMAIL
        userController.emailHello() == UserServiceImpl.HELLO
        userController.helloEmail() == EmailServiceImpl.EMAIL

    }

    def "constructor di test"() {
        given:
        def packageName = "com.cc.wheel.ioc.constructor"
        BeanFactory beanFactory = new BeanFactoryImpl([packageName])

        when:
        SchoolController schoolController = beanFactory.getBean(SchoolController)

        then:
        schoolController.student() == StudentServiceImpl.STUDENT
        schoolController.teacher() == TeacherServiceImpl.TEACHER

    }


    def "constructor circle di test"() {
        given:
        def packageName = "com.cc.wheel.ioc.circle"
        BeanFactory beanFactory = new BeanFactoryImpl([packageName])

        when:
        AClass aClass = beanFactory.getBean(AClass)

        then:
        thrown(RuntimeException)

    }
}
