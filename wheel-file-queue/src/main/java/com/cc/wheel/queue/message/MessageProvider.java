package com.cc.wheel.queue.message;

import com.cc.wheel.queue.file.CircleFileQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author cc
 * @date 2023/9/20
 */
@Slf4j
public class MessageProvider implements Runnable {

    /**
     * 消息阻塞队列
     */
    private final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(128);

    private final CircleFileQueue circleFileQueue;

    public MessageProvider(CircleFileQueue circleFileQueue) {
        this.circleFileQueue = circleFileQueue;
    }

    public void putMessage(String message) throws InterruptedException {
        this.messageQueue.put(message);
    }


    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                circleFileQueue.putMessage(messageQueue.take());
            } catch (Exception e) {
                log.error("Error get message from file", e);
            }
        }
    }
}
