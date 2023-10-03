package com.cc.wheel.queue.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * @author cc
 * @date 2023/9/20
 */
@Slf4j
public class CircleFileQueue {
    private final String dataPath = "./data";
    private final String filePrefix;
    private final int fileSize;
    private volatile Thread readThread;
    private long readFileIndex;
    private long writeFileIndex;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private final AtomicLong readIndex = new AtomicLong(0);
    private final AtomicLong writeIndex = new AtomicLong(0);

    /**
     * @param filePrefix 文件前缀
     * @param fileSize   文件消息数量
     */
    public CircleFileQueue(String filePrefix, int fileSize) {
        this.filePrefix = filePrefix;
        this.fileSize = fileSize;

        File dir = new File(dataPath);
        if (dir.exists() && Objects.nonNull(dir.listFiles())) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                if (!f.delete()) {
                    throw new RuntimeException("Data dir delete fail : " + dir.getAbsolutePath());
                }
            }
        }
        if (dir.exists() && !dir.delete()) {
            throw new RuntimeException("Data dir delete fail : " + dir.getAbsolutePath());
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Data dir create fail : " + dir.getAbsolutePath());
        }
    }

    private String getFilePath(long index) {
        return dataPath + "/" + filePrefix + index;
    }

    public void putMessage(String message) throws IOException {
        long fileIndex = this.writeIndex.get() / fileSize;
        if (fileIndex != this.writeFileIndex) {
            // close the old output stream
            if (Objects.nonNull(this.outputStream)) {
                FileOutputStream toClose = this.outputStream;
                this.outputStream = null;
                toClose.close();
            }
            this.writeFileIndex = fileIndex;
        }

        if (Objects.isNull(this.outputStream)) {
            this.outputStream = new FileOutputStream(getFilePath(this.writeFileIndex));
        }
        this.outputStream.write(message.length());
        this.outputStream.write(message.getBytes(StandardCharsets.UTF_8));
        this.writeIndex.incrementAndGet();

        // unlock
        if (Objects.nonNull(this.readThread)) {
            LockSupport.unpark(this.readThread);
        }
    }

    public String takeMessage() throws IOException {
        int retry = 10;
        for (int i = 0; this.readIndex.get() == this.writeIndex.get(); i++, i %= retry) {
            if (i == retry - 1) {
                this.readThread = Thread.currentThread();
                LockSupport.park();
                this.readThread = null;
            }
        }
        long fileIndex = this.readIndex.get() / fileSize;
        if (fileIndex != this.readFileIndex) {
            // close the old input stream
            if (Objects.nonNull(this.inputStream)) {
                FileInputStream toClose = this.inputStream;
                this.inputStream = null;
                toClose.close();
            }
            File old = new File(getFilePath(readFileIndex));
            if (!old.delete()) {
                log.error("Delete old read file {} fail", old.getAbsolutePath());
            }
            this.readFileIndex = fileIndex;
        }

        if (Objects.isNull(this.inputStream)) {
            this.inputStream = new FileInputStream(dataPath + "/" + filePrefix + this.readFileIndex);
        }
        int len = this.inputStream.read();
        String res = new String(this.inputStream.readNBytes(len), StandardCharsets.UTF_8);
        this.readIndex.incrementAndGet();
        return res;
    }
}
