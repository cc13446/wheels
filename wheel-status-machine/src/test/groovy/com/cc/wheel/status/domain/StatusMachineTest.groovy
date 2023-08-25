package com.cc.wheel.status.domain

import com.cc.wheel.status.builder.StatusMachineBuilder
import spock.lang.Specification

/**
 * @author cc
 * @date 2023/8/19
 */
class StatusMachineTest extends Specification {

    def "test status machine fire"() {
        given:
        def offHookStatus = "OffHook"
        EntryCall offHookStatusEntry = (String from, String to, String event, def map) -> println("[OffHook] entry from $from to $to event $event")
        ExitCall offHookStatusExit = (String from, String to, String event, def map) -> println("[OffHook] exit from $from to $to event $event")

        def ringingStatus = "Ringing"
        EntryCall ringingStatusEntry = (String from, String to, String event, def map) -> {
            println("[Ringing] entry from $from to $to event $event")
            return "CallConnected"
        }
        ExitCall ringingStatusExit = (String from, String to, String event, def map) -> println("[Ringing] exit from $from to $to event $event")

        def connectedStatus = "Connected"
        EntryCall connectedStatusEntry = (String from, String to, String event, def map) -> println("[Connected] entry from $from to $to event $event")
        ExitCall connectedStatusExit = (String from, String to, String event, def map) -> println("[Connected] exit from $from to $to event $event")


        def callDialedEvent = "CallDialed"
        EventGuard callDialedEventGuard = (String from, String to, String event, def map) -> {
            println("[CallDialed] guard from $from to $to event $event")
            return true
        }
        EventCall callDialedEventCall = (String from, String to, String event, def map) -> {
            println("[CallDialed] call from $from to $to event $event")
            return true
        }

        def hungUpEvent = "HungUp"
        EventGuard hungUpEventGuard = (String from, String to, String event, def map) -> {
            println("[HungUp] guard from $from to $to event $event")
            return true
        }
        EventCall hungUpEventCall = (String from, String to, String event, def map) -> {
            println("[HungUp] call from $from to $to event $event")
            return true
        }

        def callConnectedEvent = "CallConnected"
        EventGuard callConnectedEventGuard = (String from, String to, String event, def map) -> {
            println("[CallConnected] guard from $from to $to event $event")
            return true
        }
        EventCall callConnectedEventCall = (String from, String to, String event, def map) -> {
            println("[CallConnected] call from $from to $to event $event")
            return true
        }

        and:
        def phoneCallConfig = new StatusMachineBuilder()

        phoneCallConfig.configInitStatus(offHookStatus)
                .addEntryCall(offHookStatusEntry)
                .addExitCall(offHookStatusExit)
                .permit(callDialedEvent, ringingStatus)

        phoneCallConfig.configStatus(ringingStatus)
                .addEntryCall(ringingStatusEntry)
                .addExitCall(ringingStatusExit)
                .permit(hungUpEvent, offHookStatus)
                .permit(callConnectedEvent, connectedStatus)

        phoneCallConfig.configStatus(connectedStatus)
                .addEntryCall(connectedStatusEntry)
                .addExitCall(connectedStatusExit)
                .permit(hungUpEvent, offHookStatus)

        phoneCallConfig.configEvent(callDialedEvent)
                .addEventGuard(callDialedEventGuard)
                .addEventCall(callDialedEventCall)
                .configTransaction(true)

        phoneCallConfig.configEvent(hungUpEvent)
                .addEventGuard(hungUpEventGuard)
                .addEventCall(hungUpEventCall)
                .configTransaction(true)

        phoneCallConfig.configEvent(callConnectedEvent)
                .addEventGuard(callConnectedEventGuard)
                .addEventCall(callConnectedEventCall)
                .configTransaction(true)

        when:
        println("start")
        StatusMachine phoneCall = phoneCallConfig.build()
        then:
        phoneCall.curStatus() == offHookStatus
        when:
        println("fire callDialed")
        // ring 会自动触发 Connected
        phoneCall.fire(callDialedEvent, ["key": "value"])
        then:
        phoneCall.curStatus() == connectedStatus
        when:
        println("fire hungUp")
        phoneCall.fire(hungUpEvent, ["key": "value"])
        then:
        phoneCall.curStatus() == offHookStatus
    }
}
