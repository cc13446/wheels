package com.cc.wheel.dubbo.extension;

import com.cc.wheel.dubbo.common.URL;
import com.cc.wheel.dubbo.extension.factory.ExtensionFactory;
import com.cc.wheel.dubbo.utils.ClassCastUtil;
import com.cc.wheel.dubbo.utils.ConcurrentHashSet;
import com.cc.wheel.dubbo.utils.Holder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * SPI扩展的核心类：扩展加载器，主要有以下功能：<br>
 * 1. 通过静态Map管理不同类的扩展加载器 <br>
 * 2.
 *
 * @author cc
 * @date 2023/7/21
 */

@Slf4j
public class ExtensionLoader<T> {

    // 静态属性和方法

    /**
     * 静态Map，存储了每个类对应的扩展集加载器
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);

    /**
     * 静态Map，缓存每个扩展的实例
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * spi 配置文件文件夹
     */
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    /**
     * spi 配置文件文件夹
     */
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    /**
     * spi 配置文件文件夹
     */
    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    /**
     * @param type 扩展类型
     * @param <T>  扩展类型
     * @return 此类型的扩展加载器
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null!");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("Extension type(" + type + ") is without @" + SPI.class.getSimpleName() + " Annotation!");
        }

        // 先从缓存里面拿
        ExtensionLoader<T> loader = ClassCastUtil.cast(EXTENSION_LOADERS.get(type));
        if (loader == null) {
            // 拿不到，建一个
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = ClassCastUtil.cast(EXTENSION_LOADERS.get(type));
        }
        return loader;
    }


    // 普通属性和方法

    /**
     * 扩展加载器管理的扩展类类型
     */
    private final Class<T> type;

    /**
     * 自适应扩展类
     */
    private volatile Class<? extends T> cachedAdaptiveClass = null;


    /**
     * 所有 wrapper 类
     */
    private Set<Class<?>> cachedWrapperClasses;

    /**
     * 所有扩展类
     */
    private final Holder<Map<String, Class<? extends T>>> cachedClasses = new Holder<>();

    /**
     * 扩展的自适应实例
     */
    private final Holder<T> cachedAdaptiveInstance = new Holder<>();

    /**
     * 创建扩展自适应实例时发生的错误
     */
    private volatile Throwable createAdaptiveInstanceError;

    /**
     * 扩展的实例缓存
     */
    private final ConcurrentMap<String, Holder<T>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * 默认扩展的名字
     */
    private String cachedDefaultName;

    /**
     * 扩展的工厂类
     */
    private final ExtensionFactory objectFactory;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
        // 因为扩展工厂本身就是一个扩展，如果这里不加判断的话，就会自己依赖自己，死循环
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    /**
     * 获取此扩展类的自适应扩展
     *
     * @return 自适应扩展
     */
    public T getAdaptiveExtension() {
        T instance = cachedAdaptiveInstance.get();
        if (Objects.isNull(instance)) {
            // 如果此字段有值，则说明已经创建过，而且失败了
            if (Objects.isNull(createAdaptiveInstanceError)) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (Objects.isNull(instance)) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t, t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError, createAdaptiveInstanceError);
            }
        }
        return instance;
    }

    /**
     * @return 创建的自适应扩展实例
     */
    private T createAdaptiveExtension() {
        try {
            // 这里对自适应扩张进行了依赖注入
            return injectExtension(getAdaptiveExtensionClass().getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * @param instance 进行依赖注入的实例
     * @return 注入之后的实例
     */
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    // 要求只有一个参数，而且是 public 的
                    if (method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers())) {

                        // 如果没有标注此注解，说明不需要注入
                        if (Objects.isNull(method.getAnnotation(EnableInject.class))) {
                            continue;
                        }

                        // 参数只有一个
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            // 根据方法名字拿到注入的扩展名字
                            String property = method.getName();
                            if (property.startsWith("set") && property.length() > 3) {
                                property = property.substring(3, 4).toLowerCase() + property.substring(4);
                            }
                            // 进行注入
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            log.error("fail to inject via method " + method.getName() + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return instance;
    }

    /**
     * @return 解析到的自适应扩展类
     */
    private Class<? extends T> getAdaptiveExtensionClass() {
        // 触发解析流程
        loadExtensionClasses();
        if (Objects.nonNull(cachedAdaptiveClass)) {
            return cachedAdaptiveClass;
        }
        // 如果没有类上面有Adaptive注解，那就自己生成一个自适应扩展类
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    /**
     * @return 自适应扩展类
     */
    private Class<T> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        com.cc.wheel.dubbo.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(com.cc.wheel.dubbo.compiler.Compiler.class).getAdaptiveExtension();
        return ClassCastUtil.cast(compiler.compile(code, classLoader));
    }


    /**
     * 加载所有的扩展类
     */
    private void loadExtensionClasses() {
        Map<String, Class<? extends T>> classes = cachedClasses.get();
        if (Objects.isNull(classes)) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (Objects.isNull(classes)) {
                    classes = doLoadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
    }

    /**
     * @return 默认扩展
     */
    public T getDefaultExtension() {
        loadExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     * @param name 扩展名字
     * @return 扩展
     */
    public T getExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Extension name is blank");
        }

        Holder<T> holder = cachedInstances.get(name);
        if (Objects.isNull(holder)) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        T instance = holder.get();
        if (Objects.isNull(instance)) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return instance;
    }

    /**
     * @return 扩展的实现类
     */
    private Map<String, Class<? extends T>> getExtensionClasses() {
        Map<String, Class<? extends T>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return cachedClasses.get();
    }

    /**
     * @param name 扩展名字
     * @return 扩展
     */
    private T createExtension(String name) {
        Class<? extends T> clazz = getExtensionClasses().get(name);
        if (Objects.isNull(clazz)) {
            throw new IllegalStateException("no such extension " + type.getSimpleName() + " by name " + name);
        }
        try {
            T instance = ClassCastUtil.cast(EXTENSION_INSTANCES.get(clazz));
            if (Objects.isNull(instance)) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = ClassCastUtil.cast(EXTENSION_INSTANCES.get(clazz));
            }
            // 注入属性
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            // 这里实现了 aop
            if (CollectionUtils.isNotEmpty(wrapperClasses)) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension(ClassCastUtil.cast(wrapperClass.getConstructor(type).newInstance(instance)));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " + type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    /**
     * @return 所有的扩展类
     */
    private Map<String, Class<? extends T>> doLoadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (Objects.nonNull(defaultAnnotation)) {
            String value = defaultAnnotation.value().trim();
            if (StringUtils.isNotBlank(value)) {
                // dubbo 允许用逗号分割多个name，这里简化了
                cachedDefaultName = value;
            }
        }

        Map<String, Class<? extends T>> extensionClasses = new HashMap<>();
        loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadDirectory(extensionClasses, DUBBO_DIRECTORY);
        loadDirectory(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    /**
     * 拿到文件夹下配置文件中的所有扩展
     *
     * @param extensionClasses 扩展类集合
     * @param dir              文件夹
     */
    private void loadDirectory(Map<String, Class<? extends T>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: " + type + ", description file: " + fileName + ").", t);
        }
    }

    /**
     * @param extensionClasses 扩展类集合
     * @param classLoader      类加载器
     * @param resourceURL      url
     */
    private void loadResource(Map<String, Class<? extends T>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 注释的 index
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            name = line.substring(0, i).trim();
                            line = line.substring(i + 1).trim();
                        }
                        if (line.length() > 0) {
                            loadClass(extensionClasses, resourceURL, ClassCastUtil.cast(Class.forName(line, true, classLoader)), name);
                        }
                    } catch (Throwable t) {
                        throw new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                    }
                }
            }

        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: " + type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    /**
     * @param extensionClasses 扩展类集合
     * @param resourceURL      url
     * @param clazz            反射类
     * @param name             扩展名字
     * @throws NoSuchMethodException 是否有默认构造方法
     */
    private void loadClass(Map<String, Class<? extends T>> extensionClasses, java.net.URL resourceURL, Class<? extends T> clazz, String name) throws NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " + type + ", class line: " + clazz.getName() + "), class " + clazz.getName() + "is not subtype of interface.");
        }
        // 是不是自适应扩展类
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            if (cachedAdaptiveClass == null) {
                cachedAdaptiveClass = clazz;
            } else if (!cachedAdaptiveClass.equals(clazz)) {
                throw new IllegalStateException("More than 1 adaptive class found: " + Class.class.getName() + ", " + Class.class.getName());
            }
            // 是不是wrapper
        } else if (isWrapperClass(clazz)) {
            Set<Class<?>> wrappers = cachedWrapperClasses;
            if (wrappers == null) {
                cachedWrapperClasses = new ConcurrentHashSet<>();
                wrappers = cachedWrapperClasses;
            }
            wrappers.add(clazz);
        } else {
            // 这里验证是否有默认构造方法
            clazz.getConstructor();
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
            }
            Class<?> c = extensionClasses.get(name);
            if (Objects.isNull(c)) {
                extensionClasses.put(name, clazz);
            } else if (!c.equals(clazz)) {
                throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName());
            }
        }
    }

    /**
     * @param clazz 扩展类
     * @return 此扩展类是不是 wrapper
     */
    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


    /**
     * @return 支持扩展的所有名字
     */
    public Set<String> getSupportedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<>(getExtensionClasses().keySet()));
    }

    /**
     * @return 生成的自适应扩展类的代码
     */
    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveAnnotation) {
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");
        }

        codeBuilder.append("package ").append(type.getPackage().getName()).append(";");
        codeBuilder.append("\nimport ").append(ExtensionLoader.class.getName()).append(";");
        codeBuilder.append("\npublic class ").append(type.getSimpleName()).append("$Adaptive").append(" implements ").append(type.getCanonicalName()).append(" {");

        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                // 如果不是自适应扩展的方法，不支持调用
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                // 找到第几个参数是 URL
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // found parameter in URL type
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                }
                // did not find parameter in URL type
                else {
                    String attribMethod = null;

                    // find URL getter method
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adaptive class for interface " + type.getName() + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    // Null point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");", urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");", urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }

                String[] value = adaptiveAnnotation.value();
                // value is not set, use the value generated from class name as the key
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null
                String s = String.format("\nif(extName == null) " + "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");", type.getName(), Arrays.toString(value));
                code.append(s);

                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);", type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append("\npublic ").append(rt.getCanonicalName()).append(" ").append(method.getName()).append("(");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(pts[i].getCanonicalName());
                codeBuilder.append(" ");
                codeBuilder.append("arg").append(i);
            }
            codeBuilder.append(")");
            if (ets.length > 0) {
                codeBuilder.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuilder.append(", ");
                    }
                    codeBuilder.append(ets[i].getCanonicalName());
                }
            }
            codeBuilder.append(" {");
            codeBuilder.append(code);
            codeBuilder.append("\n}");
        }
        codeBuilder.append("\n}");
        if (log.isDebugEnabled()) {
            log.debug(codeBuilder.toString());
        }
        return codeBuilder.toString();
    }

}
