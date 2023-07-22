package com.cc.wheel.dubbo.extension;

import java.lang.annotation.*;

/**
 * 标注在方法上，代表扩展实现类的这个方法需要依赖注入 <br>
 * dubbo 里面是禁止注入，这里改成了标注则是需要注入
 * @author cc
 * @date 2023/7/22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EnableInject {
}
