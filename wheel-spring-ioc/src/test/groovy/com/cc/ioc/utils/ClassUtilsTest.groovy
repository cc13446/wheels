package com.cc.ioc.utils

import com.cc.ioc.core.BeanFactory
import com.cc.ioc.core.impl.BeanFactoryImpl
import spock.lang.Specification


/**
 * @author cc
 * @date 2023/9/30
 */
class ClassUtilsTest extends Specification {

    def "find class test"() {
        given:
        def packageName = "com.cc.ioc.core"

        when:
        def res = ClassUtils.findClass(packageName, (c) -> true)

        then:
        res.size() == 2
        res.contains(BeanFactory)
        res.contains(BeanFactoryImpl)
    }
}
