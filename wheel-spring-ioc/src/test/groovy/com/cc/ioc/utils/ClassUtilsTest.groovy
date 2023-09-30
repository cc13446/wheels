package com.cc.ioc.utils

import com.cc.ioc.annotation.Component
import com.cc.ioc.example.user.User
import com.cc.ioc.example.user.impl.UserImpl
import spock.lang.Specification

/**
 * @author cc
 * @date 2023/9/30
 */
class ClassUtilsTest extends Specification {

    def "find class test"() {
        given:
        def packageName = "com.cc.ioc.example.user"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> true)

        then:
        res.size() == 2
        res.contains(UserImpl)
        res.contains(User)
    }

    def "find class test filter"() {
        given:
        def packageName = "com.cc.ioc.example.user"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> c.isAnnotationPresent(Component))

        then:
        res.size() == 1
        res.contains(UserImpl)
    }


    def "get classes name test"() {
        when:
        def res = ClassUtils.getClassNames(UserImpl)

        then:
        res.size() == 1
        res.contains(UserImpl.getName())
    }

    def "get interfaces name test"() {
        when:
        def res = ClassUtils.getInterfaceNames(UserImpl)

        then:
        res.size() == 1
        res.contains(User.getName())
    }
}
