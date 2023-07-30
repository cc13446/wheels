# wheel-dubbo-spi
本模块旨在模仿`dubbo`的`spi`扩展机制，注意：本模块基于`dubbo 2.6.x`

## 扩展机制
`dubbo`的 `spi` 扩展机制和 `java` 的 `spi` 扩展机制类似，都是由服务端定义接口，客户端进行实现，从而达到定义和实现解耦的目的。

区别就在于 `java` 的 `spi` 扩展会所有的实现，而`dubbo`的扩展机制会为每一个扩展的实现取一个名字，当需要使用哪一个实现的时候，根据名字获取实现就可以了。

除此以外，`dubbo`的`spi`扩展还有其他的一些功能，比如：

- 自适应扩展类：根据传入`url`参数自动使用响应的扩展
- `AOP`实现：使用`Wrapper`来实现对扩展的`AOP`
- `IOC`实现：可以进行`set`方法注入

## Holder机制

详情可以参照`com.cc.wheel.dubbo.utils.Holder<T>`

当某个字段需要延迟初始化，又因为并发问题需要进行双重校验的时候，此机制可能会有帮助，可以减少加锁的范围。

## 统一资源描述

详情可以参考`com.cc.wheel.dubbo.common.URL`

这是`dubbo`的统一资源描述模型，可以快速的描述一个资源，并方便的进行序列化和反序列化。

## 线程安全的HashSet

详情参考`com.cc.wheel.dubbo.utils.ConcurrentHashSet<E>`

简单复用了`ConcurrentHashMap`

## 强制类型转换工具

详情参考`com.cc.wheel.dubbo.utils.ClassCastUtil`

避免消除警告的注解满天飞。
