package com.cc.wheel.ioc.utils

import com.cc.wheel.ioc.annotation.Component
import com.cc.wheel.ioc.property.service.UserService
import com.cc.wheel.ioc.property.service.impl.EmailServiceImpl
import com.cc.wheel.ioc.property.service.impl.UserServiceImpl
import spock.lang.Specification

/**
 * @author cc
 * @date 2023/9/30
 */
class ClassUtilsTest extends Specification {

    def "find class test"() {
        given:
        def packageName = "com.cc.wheel.ioc.property.service"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> true)

        then:
        res.size() == 4
    }

    def "find class test filter"() {
        given:
        def packageName = "com.cc.wheel.ioc.property.service"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> c.isAnnotationPresent(Component))

        then:
        res.size() == 2
        res.contains(UserServiceImpl)
        res.contains(EmailServiceImpl)
    }


    def "get classes name test"() {
        when:
        def res = ClassUtils.getClassNames(UserServiceImpl)

        then:
        res.size() == 1
        res.contains(UserServiceImpl.getName())
    }

    def "get interfaces name test"() {
        when:
        def res = ClassUtils.getInterfaceNames(UserServiceImpl)

        then:
        res.size() == 1
        res.contains(UserService.getName())
    }
}
