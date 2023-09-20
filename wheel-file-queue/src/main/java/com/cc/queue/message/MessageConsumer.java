package com.cc.queue.message;

import com.cc.queue.file.CircleFileQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author cc
 * @date 2023/9/20
 */
@Slf4j
public class MessageConsumer implements Runnable {

    /**
     * 消息阻塞队列
     */
    private final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(128);

    private final CircleFileQueue circleFileQueue;

    public MessageConsumer(CircleFileQueue circleFileQueue) {
        this.circleFileQueue = circleFileQueue;
    }

    public String takeMessage() throws InterruptedException {
        return messageQueue.take();
    }


    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                messageQueue.put(circleFileQueue.takeMessage());
            } catch (Exception e) {
                log.error("Error get message from file", e);
            }
        }
    }
}
