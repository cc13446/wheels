package com.cc.wheel.timer;

import java.util.Objects;
import java.util.Set;

/**
 * 时间轮数组
 *
 * @author cc
 * @date 2024/2/24
 */
public class HashedWheelBucket {

    // 双向链表头尾
    private HashedWheelTimeout head;
    private HashedWheelTimeout tail;

    /**
     * 添加定时任务
     */
    public void addTimeout(HashedWheelTimeout timeout) {
        assert Objects.isNull(timeout.bucket) : "重复添加定时任务";
        timeout.bucket = this;
        if (Objects.isNull(head)) {
            head = tail = timeout;
        } else {
            tail.next = timeout;
            timeout.prev = tail;
            tail = timeout;
        }
    }

    /**
     * 删除定时任务
     */
    public HashedWheelTimeout remove(HashedWheelTimeout timeout) {
        HashedWheelTimeout next = timeout.next;
        if (Objects.nonNull(timeout.prev)) {
            timeout.prev.next = next;
        }
        if (Objects.nonNull(timeout.next)) {
            timeout.next.prev = timeout.prev;
        }
        if (timeout == head) {
            if (timeout == tail) {
                tail = null;
                head = null;
            } else {
                head = next;
            }
        } else if (timeout == tail) {
            tail = timeout.prev;
        }
        timeout.prev = null;
        timeout.next = null;
        timeout.bucket = null;
        timeout.timer.decrementPendingTimeouts();
        return next;
    }

    /**
     * 执行定时任务
     *
     * @param deadline 到期时间
     */
    public void expireTimeouts(long deadline) {
        // 获取链表的头节点
        HashedWheelTimeout timeout = head;
        // 开始处理定时任务
        while (Objects.nonNull(timeout)) {
            // 先得到下一个定时任务
            HashedWheelTimeout next = timeout.next;
            // 剩余轮数小于0就说明这一轮就可以执行该定时任务了
            if (timeout.remainingRounds <= 0) {
                // 把该定时任务从双向链表中删除，该方法的返回结果是下一个节点
                next = remove(timeout);
                if (timeout.deadline <= deadline) {
                    // 执行定时任务
                    timeout.expire();
                } else {
                    throw new IllegalStateException(String.format("timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
                }
            } else if (timeout.isCancelled()) {
                // 如果定时任务被取消了，从双向链表中删除该任务
                next = remove(timeout);
            } else {
                // 走到这里说明remainingRounds还大于0，这就意味着还不到执行定时任务的轮数，轮数减1即可
                timeout.remainingRounds--;
            }
            // 向后遍历双向链表
            timeout = next;
        }
    }

    /**
     * 清理所有定时任务
     *
     * @param set 目前有效定时任务集合
     */
    public void clearTimeouts(Set<Timeout> set) {
        for (; ; ) {
            HashedWheelTimeout timeout = pollTimeout();
            if (Objects.isNull(timeout)) {
                return;
            }
            if (timeout.isExpired() || timeout.isCancelled()) {
                continue;
            }
            set.add(timeout);
        }
    }

    /**
     * 获取下一个定时任务
     *
     * @return 定时任务
     */
    private HashedWheelTimeout pollTimeout() {
        HashedWheelTimeout head = this.head;
        if (Objects.isNull(head)) {
            return null;
        }
        HashedWheelTimeout next = head.next;
        if (Objects.isNull(next)) {
            tail = this.head = null;
        } else {
            this.head = next;
            next.prev = null;
        }
        //帮助gc
        head.next = null;
        head.prev = null;
        head.bucket = null;
        return head;
    }

}
