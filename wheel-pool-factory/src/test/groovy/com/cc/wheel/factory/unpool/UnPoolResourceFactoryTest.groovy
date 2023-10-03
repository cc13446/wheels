package com.cc.wheel.factory.unpool

import com.cc.wheel.factory.ResourceWrapper
import com.cc.wheel.factory.TestResourceWrapper
import spock.lang.Specification

/**
 * @author: cc
 * @date: 2023/8/27 
 */
class UnPoolResourceFactoryTest extends Specification {

    def "test unpool resource factory raw fail"() {
        given:
        def factory = new UnPoolResourceFactory(new Properties(), ResourceWrapper)

        when:
        factory.getResource()

        then:
        thrown(RuntimeException)

    }

    def "test unpool resource factory"() {
        given:
        Integer value = 1
        def properties = new Properties()
        properties.put(TestResourceWrapper.KEY, value)
        def factory = new UnPoolResourceFactory(properties, TestResourceWrapper)

        when:
        ResourceWrapper<Integer> resourceWrapper = factory.getResource()

        then:
        resourceWrapper.getResource() == value

        when:
        resourceWrapper.close()

        then:
        resourceWrapper.getResource() == null

    }
}
