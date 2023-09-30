package com.cc.ioc.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 工具类
 *
 * @author cc
 * @date 2023/9/30
 */
public class ClassUtils {

    /**
     * The package separator character: {@code '.'}.
     */
    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * The path separator character: {@code '/'}.
     */
    private static final char PATH_SEPARATOR = '/';

    /**
     * The class suffix: {@code ".class"}.
     */
    private static final String CLASS_SUFFIX = ".class";

    /**
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        assert StringUtils.isNoneBlank(className) : "Class name must not be null";
        return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * @param packageName 包名
     * @param filter      class 过滤
     * @return 包下的所有符合条件的类
     * @throws ClassNotFoundException class not found
     */
    public static List<Class<?>> findClass(String packageName, Function<Class<?>, Boolean> filter) throws ClassNotFoundException {
        List<Class<?>> res = new ArrayList<>(128);

        // 将包名替换成目录
        String fileName = convertClassNameToResourcePath(packageName);

        // 通过classloader来获取文件列表
        URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
        assert Objects.nonNull(resource) : "The package " + packageName + " not found";

        // 打开文件
        File file = new File(resource.getFile());

        // 获取包下的类
        doFindClass(packageName, file, filter, res);
        return res;
    }

    /**
     * @param packageName 包名
     * @param file        文件
     * @param filter      class 过滤
     * @param res         结果
     * @throws ClassNotFoundException class not found
     */
    private static void doFindClass(String packageName, File file, Function<Class<?>, Boolean> filter, List<Class<?>> res) throws ClassNotFoundException {
        File[] files = file.listFiles();
        if (Objects.isNull(files)) {
            return;
        }
        for (File f : files) {
            // 如果是目录，递归查找
            if (f.isDirectory()) {
                doFindClass(concatPackageName(packageName, f.getName()), f, filter, res);
            } else if (f.getName().endsWith(CLASS_SUFFIX)) {
                // 如果是类文件, 反射出实例
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(concatClassName(packageName, f.getName()));
                if (Boolean.TRUE.equals(filter.apply(clazz))) {
                    res.add(clazz);
                }
            }
        }
    }

    /**
     * @param basePackageName 包名
     * @param subPackageName  子包名
     * @return 子包全名
     */
    private static String concatPackageName(String basePackageName, String subPackageName) {
        return basePackageName + PACKAGE_SEPARATOR + subPackageName;
    }

    /**
     * @param basePackageName 包名
     * @param fileName        文件名
     * @return 子包全名
     */
    private static String concatClassName(String basePackageName, String fileName) {
        return basePackageName + PACKAGE_SEPARATOR + fileName.replace(CLASS_SUFFIX, StringUtils.EMPTY);
    }
}
