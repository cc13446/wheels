package com.cc.wheel.pool;

import com.cc.wheel.ResourceWrapper;
import lombok.AllArgsConstructor;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.cc.wheel.pool.PoolResourceFactory.CLOSE;

/**
 * @author: cc
 * @date: 2023/8/27
 */
@AllArgsConstructor
public class ResourceProxy<T> implements MethodInterceptor {

    private final ResourceWrapper<T> object;

    private final PoolResourceFactory<T> factory;

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String methodName = method.getName();
        if (CLOSE.equals(methodName)) {
            factory.returnResource(object);
            return null;
        }
        try {
            return method.invoke(object, args);
        } catch (Throwable t) {
            throw new RuntimeException("Invoke resource wrapper method fail", t);
        }
    }
}
