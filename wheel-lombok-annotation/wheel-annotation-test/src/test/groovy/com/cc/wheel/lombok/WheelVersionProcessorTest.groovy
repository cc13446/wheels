package com.cc.wheel.lombok

import spock.lang.Specification


/**
 * @author cc
 * @date 2023/10/3
 */
class WheelVersionProcessorTest extends Specification{

    def "test wheel version annotation" () {
        expect:
        Test.VERSION == WheelVersionProcessor.VERSION
    }
}
