package com.cc.wheel.queue;

/**
 * @author cc
 * @date 2023/9/20
 */
public interface FileQueue {

    /**
     * put a message into queue, maybe block when the message is too many to append to the circle file;
     * @param message the message
     * @throws InterruptedException while the thread is interrupted
     */
    void put(String message) throws InterruptedException;

    /**
     * take a message from queue, maybe block when there is no message in the circle file;
     * @return a message
     * @throws InterruptedException while the thread is interrupted
     */
    String take() throws InterruptedException;

}
