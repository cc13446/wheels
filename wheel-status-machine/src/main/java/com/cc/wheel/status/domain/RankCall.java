package com.cc.wheel.status.domain;

/**
 * @author cc
 * @date 2023/8/19
 */
public interface RankCall {

    /**
     * 顺序
     * @return 调用顺序从小到大
     */
    default int rank() {
        return 0;
    }

}
