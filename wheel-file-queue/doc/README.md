# wheel-file-queue
本模块旨在完成一个利用文件的消息队列。

利用一组文件循环的方式，消费者从文件头读取，生产者从文件末尾写入，实现持久化的消息队列。