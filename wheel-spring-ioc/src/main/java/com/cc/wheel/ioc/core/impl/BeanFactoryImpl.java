package com.cc.wheel.ioc.core.impl;

import com.cc.wheel.ioc.annotation.Autowired;
import com.cc.wheel.ioc.annotation.Component;
import com.cc.wheel.ioc.annotation.Primary;
import com.cc.wheel.ioc.annotation.Qualifier;
import com.cc.wheel.ioc.bean.BeanDefinition;
import com.cc.wheel.ioc.bean.ConstructorDefinition;
import com.cc.wheel.ioc.bean.PropertyDefinition;
import com.cc.wheel.ioc.bean.base.NameDefinition;
import com.cc.wheel.ioc.core.BeanFactory;
import com.cc.wheel.ioc.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;

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
     * All resolved bean definitions
     */
    private Map<String, BeanDefinition> resolverBeanDefinitionMap;

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
            resolveBeanDefinitions();
            doDI();
        }
        init = true;
    }

    /**
     * 解析所有的 beanDefinition
     */
    private void resolveBeanDefinitions() {
        Map<String, BeanDefinition> res = new HashMap<>();
        BiConsumer<String, BeanDefinition> processBeanDefinition = (name, bf) -> {
            if (StringUtils.isNoneBlank(name)) {
                res.compute(name, (key, old) -> {
                    if (Objects.isNull(old)) {
                        return bf;
                    } else if (bf.isPrimary()) {
                        if (old.isPrimary()) {
                            throw new RuntimeException("There has two primary bean at name : " + key);
                        }
                        return bf;
                    } else {
                        return old;
                    }
                });
            }
        };

        for (BeanDefinition beanDefinition : this.beanDefinitions) {
            String name = beanDefinition.getName();
            processBeanDefinition.accept(name, beanDefinition);
            for (String n : beanDefinition.getClassNames()) {
                processBeanDefinition.accept(n, beanDefinition);
            }
            for (String n : beanDefinition.getInterfaceNames()) {
                processBeanDefinition.accept(n, beanDefinition);
            }
        }
        this.resolverBeanDefinitionMap = Collections.unmodifiableMap(res);
    }

    /**
     * 进行依赖注入
     */
    private void doDI() {
        for (BeanDefinition bf : this.resolverBeanDefinitionMap.values()) {
            // has been DI
            if (Objects.nonNull(bf.getValue())) {
                continue;
            }
            // 先创建
            if (Objects.isNull(bf.getPreValue())) {
                createBean(bf);
            }
            // 注入依赖
            injectBean(bf);
        }
    }

    /**
     * 注入bean 依赖
     *
     * @param bf bean definition
     */
    private void injectBean(BeanDefinition bf) {
        assert Objects.nonNull(bf.getPreValue()) : "When inject bean, the bean must has been created";
        try {
            for (PropertyDefinition pd : bf.getPropertyDefinitions()) {
                BeanDefinition propertyBf = findBeanDefinition(pd);
                if (Objects.isNull(propertyBf.getPreValue())) {
                    createBean(propertyBf);
                }
                pd.getMethod().invoke(bf.getPreValue(), propertyBf.getPreValue());
            }

            bf.setValue(bf.getPreValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取一个bean definition
     *
     * @param nameDefinition name
     * @return bean definition
     */
    private BeanDefinition findBeanDefinition(NameDefinition nameDefinition) {
        assert Objects.nonNull(this.resolverBeanDefinitionMap) : "When find a bean definition, the map must not be empty";
        if (StringUtils.isNoneBlank(nameDefinition.getName())) {
            BeanDefinition res = this.resolverBeanDefinitionMap.get(nameDefinition.getName());
            if (Objects.isNull(res)) {
                throw new RuntimeException("No bean name of " + nameDefinition.getName());
            }
            return res;
        }
        BeanDefinition res;
        for (String name : nameDefinition.getClassNames()) {
            res = this.resolverBeanDefinitionMap.get(name);
            if (Objects.nonNull(res)) {
                return res;
            }
        }

        for (String name : nameDefinition.getInterfaceNames()) {
            res = this.resolverBeanDefinitionMap.get(name);
            if (Objects.nonNull(res)) {
                return res;
            }
        }
        throw new RuntimeException("No bean name of " + nameDefinition);
    }

    /**
     * 创建bean
     *
     * @param bf bean definition
     */
    private void createBean(BeanDefinition bf) {
        bf.setCreating(true);
        try {
            List<ConstructorDefinition> constructorDefinitions = bf.getConstructorDefinitions();
            Class<?>[] paramTypes = new Class<?>[constructorDefinitions.size()];
            Object[] params = new Object[constructorDefinitions.size()];
            for (int i = 0; i < constructorDefinitions.size(); i++) {
                ConstructorDefinition cd = constructorDefinitions.get(i);
                BeanDefinition paramBf = findBeanDefinition(cd);
                if (paramBf.isCreating()) {
                    throw new RuntimeException("There has a circular dependency " + bf.getType().getName() + " <-> " + paramBf.getType().getName());
                }
                if (Objects.isNull(paramBf.getPreValue())) {
                    createBean(paramBf);
                }
                paramTypes[i] = cd.getType();
                params[i] = paramBf.getPreValue();
            }

            bf.setPreValue(bf.getType().getConstructor(paramTypes).newInstance(params));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bf.setCreating(false);
        }
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
        Set<Class<?>> classes = ClassUtils.findClass(basePackage, c -> c.isAnnotationPresent(Component.class));
        for (Class<?> beanClass : classes) {
            Component component = beanClass.getAnnotation(Component.class);
            boolean isPrimary = beanClass.isAnnotationPresent(Primary.class);

            Set<String> classNames = ClassUtils.getClassNames(beanClass);
            Set<String> interfaceNames = ClassUtils.getInterfaceNames(beanClass);

            List<ConstructorDefinition> constructorDefinitions = processConstructor(beanClass);
            List<PropertyDefinition> propertyDefinitions = processProperty(beanClass);

            candidates.add(new BeanDefinition(
                    isPrimary,
                    component.value(),
                    classNames,
                    interfaceNames,
                    constructorDefinitions,
                    propertyDefinitions,
                    beanClass));
        }
        return candidates;
    }

    /**
     * @param beanClass beanClass
     * @return set方法注入的解析
     */
    private List<PropertyDefinition> processProperty(Class<?> beanClass) {
        List<PropertyDefinition> res = new ArrayList<>();
        for (Method method : beanClass.getMethods()) {

            // 要求只有一个参数，而且是 public 的
            if (method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers())) {

                // 如果没有标注此注解，说明不需要注入
                if (Objects.isNull(method.getAnnotation(Autowired.class))) {
                    continue;
                }
                String name = Optional.ofNullable(method.getAnnotation(Qualifier.class)).map(Qualifier::value).orElse(StringUtils.EMPTY);

                // 参数只有一个
                Class<?> pt = method.getParameterTypes()[0];
                res.add(new PropertyDefinition(name, ClassUtils.getClassNames(pt), ClassUtils.getInterfaceNames(pt), method));
            }
        }
        return res;
    }

    /**
     * @param beanClass beanClass
     * @return 对bean构造器的解析
     */
    private List<ConstructorDefinition> processConstructor(Class<?> beanClass) {
        List<ConstructorDefinition> res = new ArrayList<>();
        Constructor<?> beanConstructor = null;
        // 如果有 autowire 注解
        for (Constructor<?> constructor : beanClass.getConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                beanConstructor = constructor;
            }
        }
        // 没有则使用无参构造
        if (Objects.isNull(beanConstructor)) {
            try {
                beanConstructor = beanClass.getConstructor();
                return res;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("The bean must should has a non-args constructor or autowire constructor");
            }
        }

        Class<?>[] params = beanConstructor.getParameterTypes();
        Annotation[][] annotations = beanConstructor.getParameterAnnotations();
        assert params.length == annotations.length : "The params length should be equal to annotations length";

        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            String name = StringUtils.EMPTY;
            Annotation[] as = annotations[i];
            if (Objects.nonNull(as)) {
                for (Annotation a : as) {
                    if (a.annotationType().equals(Qualifier.class)) {
                        Qualifier q = (Qualifier) a;
                        name = q.value();
                        break;
                    }
                }
            }
            res.add(new ConstructorDefinition(
                    name,
                    ClassUtils.getClassNames(param),
                    ClassUtils.getInterfaceNames(param),
                    param));
        }
        return res;
    }


    @Override
    public <T> T getBean(String name, Class<T> tClass) {
        if (!init) {
            init();
        }
        BeanDefinition bf = findBeanDefinition(new NameDefinition(name, ClassUtils.getClassNames(tClass), ClassUtils.getInterfaceNames(tClass)));
        if (Objects.isNull(bf)) {
            return null;
        }
        assert tClass.isAssignableFrom(bf.getType()) : "The bean should be assignable to class";
        return tClass.cast(bf.getValue());
    }

    @Override
    public <T> T getBean(Class<T> tClass) {
        return this.getBean(StringUtils.EMPTY, tClass);
    }
}
