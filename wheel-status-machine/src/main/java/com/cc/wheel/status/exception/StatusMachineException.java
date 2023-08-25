package com.cc.wheel.status.exception;

/**
 * @author cc
 * @date 2023/8/19
 */

public class StatusMachineException extends RuntimeException {
    public StatusMachineException() {
        super();
    }

    public StatusMachineException(String message) {
        super(message);
    }

}
