package com.cc.wheel.loader;

import com.cc.wheel.loader.api.Bundle;
import com.cc.wheel.loader.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * bundle 类加载器
 *
 * @author cc
 * @date 2024/3/16
 */
@Slf4j
public class BundleClassLoader extends ClassLoader {

    private final static String BUNDLE_PROPERTIES = "bundle.properties";

    private final static String CLASS_SUFFIX = ".class";

    private final String name;

    private final String jarPath;

    private final BundleProperty bundleProperty = new BundleProperty();

    private final Map<String, byte[]> classCache = new HashMap<>();

    private Bundle bundle;

    public BundleClassLoader(ClassLoader parent, String name, String jarPath) {
        super(parent);
        this.name = name;
        this.jarPath = jarPath;
        AssertUtils.assertNonBlank(name, "Bundle name cannot be blank");
        AssertUtils.assertNonBlank(jarPath, "Bundle jarPath cannot be blank");
        try (JarFile jarFile = new JarFile(jarPath)) {
            JarEntry propertyEntry = jarFile.getJarEntry(BUNDLE_PROPERTIES);
            buildBundleProperties(jarFile, AssertUtils.assertNonNull(propertyEntry, String.format("Cannot find [%s]", BUNDLE_PROPERTIES)));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                info("Loader jar entry {}", entry.getName());
                if (entry.getName().endsWith(CLASS_SUFFIX)) {
                    buildClassCache(jarFile, entry);
                }
            }
        } catch (IOException e) {
            info("Open jar file [{}] error {}", jarPath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析所有的class文件
     *
     * @param jarFile jar file
     * @param entry   class file entry
     * @throws IOException ioException
     */
    private void buildClassCache(JarFile jarFile, JarEntry entry) throws IOException {
        try (InputStream inputStream = jarFile.getInputStream(entry);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            String className = StringUtils.replaceChars(entry.getName(), '/', '.');
            className = className.substring(0, className.length() - CLASS_SUFFIX.length());
            info("Find class [{}]", className);
            this.classCache.put(className, readClassFile(bufferedInputStream));
        }
    }

    /**
     * @param bufferedInputStream class 文件的 bufferedInputStream
     * @return class文件中的内容
     */
    private byte[] readClassFile(BufferedInputStream bufferedInputStream) throws IOException {
        int index = 0;
        byte[] result = new byte[4096];

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            // 如果超过了就扩容
            int newIndex = index + bytesRead;
            if (newIndex > result.length) {
                byte[] newResult = new byte[Math.max(result.length * 2, newIndex)];
                System.arraycopy(result, 0, newResult, 0, index);
                result = newResult;
            }
            // 将读取的数据复制到结果中
            System.arraycopy(buffer, 0, result, index, bytesRead);
            index = newIndex;
        }

        byte[] res = new byte[index];
        System.arraycopy(result, 0, res, 0, index);
        return res;
    }

    /**
     * 解析模块参数
     *
     * @param jarFile jar file
     * @param entry   bundle propertied entry
     * @throws IOException ioException
     */
    private void buildBundleProperties(JarFile jarFile, JarEntry entry) throws IOException {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            this.bundleProperty.setBundleImpl(AssertUtils.assertNonBlank(properties.getProperty(BundleProperty.BUNDLE_IMPL), "Property bundle_impl cannot be blank"));
            this.bundleProperty.setPrefix(AssertUtils.assertNonBlank(properties.getProperty(BundleProperty.ISOLATED_PREFIX), "Property isolated_prefix cannot be blank"));
        }
    }

    /**
     * 输出日志
     *
     * @param message message
     * @param args    args
     */
    private void info(String message, Object... args) {
        log.info("[Bundle Class Loader : {}] : " + message, name, args);
    }

    /**
     * @return Bundle
     */
    public Bundle getBundle() {
        if (Objects.isNull(bundle)) {
            synchronized (this) {
                if (Objects.isNull(bundle)) {
                    this.bundle = getBundle0();
                }
            }
        }
        return bundle;
    }

    /**
     * 获取bundle的真正逻辑
     *
     * @return Bundle
     */
    private Bundle getBundle0() {
        try {
            Class<?> clazz = this.loadClass(this.bundleProperty.getBundleImpl());
            Object object = clazz.getConstructor().newInstance();
            if (object instanceof Bundle) {
                return (Bundle) object;
            }
            throw new RuntimeException("Bundle must impl " + Bundle.class.getName());
        } catch (Exception e) {
            this.info("Create bundle fail, bundle_impl = [{}]", this.bundleProperty.getBundleImpl());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassLoader system = getSystemClassLoader();
        // 需要隔离的类，打破双亲委派，先去jar包中找
        if (name.startsWith(this.bundleProperty.getPrefix())) {
            return findClass(name);
        }
        try {
            return system.loadClass(name);
        } catch (Exception e) {
            return findClass(name);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.classCache.getOrDefault(name, null);
        if (Objects.isNull(data)) {
            throw new ClassNotFoundException(name);
        }
        return this.defineClass(name, data, 0, data.length);
    }
}
