package com.cc.wheel.factory.pool

import com.cc.wheel.factory.TestResourceWrapper
import spock.lang.Specification

/**
 * @author: cc
 * @date: 2023/8/27 
 */
class PoolResourceFactoryTest extends Specification {

    def "test poll resource factory pool close"() {
        given:
        def one = new Properties()
        one.put(TestResourceWrapper.KEY, 1)

        def factory = new PoolResourceFactory(10, one, TestResourceWrapper)

        when:
        def res = factory.getResource()

        then:
        res.getResource() == 1

        when:
        res.close()

        then:
        res.getResource() == null
        factory.count() == 1

        when:
        res = factory.getResource()

        then:
        res.getResource() == 1
        factory.count() == 1

    }
}
