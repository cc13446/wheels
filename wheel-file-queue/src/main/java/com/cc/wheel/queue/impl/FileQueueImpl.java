package com.cc.wheel.queue.impl;

import com.cc.wheel.queue.FileQueue;
import com.cc.wheel.queue.file.CircleFileQueue;
import com.cc.wheel.queue.message.MessageConsumer;
import com.cc.wheel.queue.message.MessageProvider;
import com.cc.wheel.queue.utils.ThreadPoolUtils;

/**
 * @author cc
 * @date 2023/9/20
 */
public class FileQueueImpl implements FileQueue {

    private final MessageProvider provider;

    private final MessageConsumer consumer;

    public FileQueueImpl() {
        this("File-Queue-", 128 * 1024);
    }

    public FileQueueImpl(String filePrefix, int fileSize) {
        CircleFileQueue circleFileQueue = new CircleFileQueue(filePrefix, fileSize);
        provider = new MessageProvider(circleFileQueue);
        consumer = new MessageConsumer(circleFileQueue);
        ThreadPoolUtils.INS.execute(provider);
        ThreadPoolUtils.INS.execute(consumer);
    }

    @Override
    public void put(String message) throws InterruptedException {
        provider.putMessage(message);
    }

    @Override
    public String take() throws InterruptedException {
        return consumer.takeMessage();
    }
}
