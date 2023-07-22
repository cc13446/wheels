package com.cc.wheel.dubbo.utils;

/**
 * Helper Class for hold a value <br>
 * 当某个成员变量不能在实例创建的时候就初始化，此时这个成员变量可能是 null <br>
 * 当要初始化此成员变量的时候，可能需要加上双重校验来保证线程安全 <br>
 * 如果要加到整个实例上，范围就太大了 <br>
 * 此时就需要一个 holder，把锁加到 holder 上就好了
 * @author cc
 * @date 2023/7/22
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}