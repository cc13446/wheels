package com.cc.ioc.utils

import com.cc.ioc.annotation.Bean
import com.cc.ioc.example.user.User
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
        res.size() == 1
        res.contains(User)
    }

    def "find class test filter"() {
        given:
        def packageName = "com.cc.ioc.example.user"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> c.isAnnotationPresent(Bean))

        then:
        res.size() == 1
        res.contains(User)
    }
}
