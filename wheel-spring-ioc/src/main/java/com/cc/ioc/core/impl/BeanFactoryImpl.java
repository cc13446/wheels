package com.cc.ioc.core.impl;

import com.cc.ioc.bean.BeanDefinition;
import com.cc.ioc.core.BeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean 工厂的实现
 *
 * @author cc
 * @date 2023/9/30
 */
@Slf4j
public class BeanFactoryImpl implements BeanFactory {

    /**
     * The packages to check for annotated classes
     */
    private final List<String> basePackages;

    /**
     * Has been init
     */
    private volatile boolean init;

    /**
     * All bean definitions
     */
    private Set<BeanDefinition> beanDefinitions;

    /**
     * @param basePackages The packages to check for annotated classes
     */
    public BeanFactoryImpl(List<String> basePackages) {
        this.basePackages = Collections.unmodifiableList(basePackages);
    }

    /**
     * 初始化
     */
    private synchronized void init() {
        if (init) {
            return;
        } else {
            doScan();
        }
        init = true;
    }

    /**
     * Scan the component in base packages
     */
    private void doScan() {
        assert CollectionUtils.isNotEmpty(this.basePackages) : "The base packages can not be empty";
        Set<BeanDefinition> beanDefinitionSet = new HashSet<>();
        for (String basePackage : this.basePackages) {
            log.info("Scan base package [{}]", basePackage);
            Set<BeanDefinition> s = findComponents(basePackage);
            beanDefinitionSet.addAll(s);
        }
        this.beanDefinitions = Collections.unmodifiableSet(beanDefinitionSet);
    }

    /**
     * Scan the component in base packages
     */
    private Set<BeanDefinition> findComponents(String basePackage) {
        Set<BeanDefinition> candidates = new HashSet<>();

        return candidates;
    }

    @Override
    public <T> T getBean(String name, Class<T> tClass) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> tClass) {
        return null;
    }
}
