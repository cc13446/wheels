package com.cc.wheel.factory.pool;

import com.cc.wheel.factory.ResourceFactory;
import com.cc.wheel.factory.ResourceWrapper;
import com.cc.wheel.factory.unpool.UnPoolResourceFactory;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 池化资源工厂
 *
 * @author: cc
 * @date: 2023/8/27
 */
@Slf4j
public class PoolResourceFactory<T> implements ResourceFactory<T> {

    public static final String CLOSE = "close";

    private final static String RESOURCE = "resource";

    private final UnPoolResourceFactory<T> factory;

    private final Class<? extends ResourceWrapper<T>> tClass;

    private final long max;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private final Queue<ResourceWrapper<T>> idleResource = new LinkedBlockingQueue<>();

    private final Queue<ResourceWrapper<T>> activeResource = new LinkedBlockingQueue<>();

    public PoolResourceFactory(long max, Properties properties, Class<? extends ResourceWrapper<T>> tClass) {
        this.max = max;
        this.factory = new UnPoolResourceFactory<>(properties, tClass);
        this.tClass = tClass;
    }


    @Override
    public ResourceWrapper<T> getResource() {
        ResourceWrapper<T> res = null;
        while (Objects.isNull(res)) {
            lock.lock();
            try {
                if (!idleResource.isEmpty()) {
                    res = idleResource.poll();
                } else if (activeResource.size() < max) {
                    res = factory.getResource();
                } else {
                    // 资源不够 等待
                    try {
                        boolean awaitSuccess = condition.await(20000, TimeUnit.MILLISECONDS);
                        if (!awaitSuccess) {
                            log.error("Await fail!");
                        }
                    } catch (InterruptedException e) {
                        // set interrupt flag
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (Objects.nonNull(res)) {
                    activeResource.offer(res);
                }
            } finally {
                lock.unlock();
            }
        }
        if (Objects.isNull(res)) {
            throw new RuntimeException("Cannot get resource from pool!");
        }

        ResourceProxy<T> resourceProxy = new ResourceProxy<>(res, this);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setCallback(resourceProxy);
        return tClass.cast(enhancer.create());
    }

    public void returnResource(ResourceWrapper<T> resourceProxy) {
        lock.lock();
        try {
            activeResource.remove(resourceProxy);
            if (idleResource.size() < max) {
                // 资源复制出来，避免close之后还能使用
                ResourceWrapper<T> newResourceWrapper = tClass.getDeclaredConstructor().newInstance();
                Field field = null;

                for (Class<?> c = tClass; !c.equals(Object.class); c = c.getSuperclass()) {
                    try {
                        field = c.getDeclaredField(RESOURCE);
                    } catch (NoSuchFieldException e) {
                        // ignore
                    }
                }
                if (Objects.isNull(field)) {
                    throw new NoSuchFieldException();
                }
                field.setAccessible(true);
                field.set(newResourceWrapper, resourceProxy.getResource());
                field.set(resourceProxy, null);

                idleResource.offer(newResourceWrapper);
                condition.signal();
            } else {
                resourceProxy.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Clone new resource wrapper fail", e);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void shutdown() {
        lock.lock();
        activeResource.forEach(ResourceWrapper::close);
        idleResource.forEach(ResourceWrapper::close);
        activeResource.clear();
        idleResource.clear();
        lock.unlock();
    }

    public int count() {
        return idleResource.size() + activeResource.size();
    }
}
