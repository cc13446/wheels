package com.cc.wheel.status.builder

import com.cc.wheel.status.exception.StatusMachineException
import spock.lang.Specification
/**
 * @author cc
 * @date 2023/8/19
 */
class StatusMachineBuilderTest extends Specification {

    def "test status machine builder status config same" () {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()
        def statusName = "testStatus"
        def statusConfig = statusMachineBuilder.configStatus(statusName)

        when:
        def sameStatusConfig = statusMachineBuilder.configStatus(statusName)

        then:
        sameStatusConfig == statusConfig
        statusMachineBuilder.hasStatus(statusName)

    }

    def "test status machine builder event config same" () {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()
        def eventName = "testEvent"
        def eventConfig = statusMachineBuilder.configEvent(eventName)

        when:
        def sameEventConfig = statusMachineBuilder.configEvent(eventName)

        then:
        sameEventConfig == eventConfig
        statusMachineBuilder.hasEvent(eventName)

    }

    def "test status machine builder status config name null" () {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()

        when:
        def _ = statusMachineBuilder.configStatus(eventName)

        then:
        def e = thrown(StatusMachineException)
        e.getMessage() == "状态名字为空!"

        where:
        eventName << [null, ""]

    }

    def "test status machine builder event config name null" () {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()

        when:
        def _ = statusMachineBuilder.configEvent(statusName)

        then:
        def e = thrown(StatusMachineException)
        e.getMessage() == "事件名字为空!"

        where:
        statusName << [null, ""]

    }

    def "test status builder permit"() {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()

        when:
        def statusName = "firstStatus"
        def eventName = "eventName"
        def nextStatusName = "secondName"
        statusMachineBuilder.configStatus(statusName)
                .permit(eventName, nextStatusName)

        then:
        statusMachineBuilder.hasStatus(statusName)
        statusMachineBuilder.hasEvent(eventName)
        statusMachineBuilder.hasStatus(nextStatusName)
    }

    def "test status builder permit double"() {
        given:
        def statusMachineBuilder = new StatusMachineBuilder()

        when:
        def statusName = "firstStatus"
        def eventName = "eventName"
        def nextStatusName = "secondName"
        statusMachineBuilder.configStatus(statusName)
                .permit(eventName, nextStatusName)
                .permit(eventName, "otherStatus")

        then:
        def e = thrown(StatusMachineException)
        e.getMessage() == "多次配置事件，并转移到不同的状态！"
    }

}
