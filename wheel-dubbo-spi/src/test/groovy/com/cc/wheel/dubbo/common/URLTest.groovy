package com.cc.wheel.dubbo.common

import spock.lang.Specification


/**
 * @author cc
 * @date 2023/7/22
 */
class URLTest extends Specification {

    String key = "key"

    String value = "value"

    def "test get parameter"() {

        given:
        URL url = new URL([key: value])

        when:
        def res = url.getParameter(key)

        then:
        res == value

    }

    def "test encode"() {
        given:
        URL url = new URL([key: value])

        when:
        def res = url.encode()

        then:
        res == """[{"$key":"$value"}]"""
    }

    def "test clone"() {
        given:
        URL url = new URL([key: value])

        when:
        def res = new URL(url.encode())

        then:
        res == url
    }
}
