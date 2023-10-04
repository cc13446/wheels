package com.cc.wheel.lombok;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import lombok.SneakyThrows;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * @author cc
 * @date 2023/10/3
 */
@SupportedAnnotationTypes("com.cc.wheel.lombok.WheelVersion")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class WheelVersionProcessor extends AbstractProcessor {

    public static final String VERSION = "v1.0.1";

    /**
     * Provides an implementation of Trees.
     */
    private JavacTrees javacTrees;

    /**
     * 用于创建一系列的语法树节点
     */
    private TreeMaker treeMaker;

    /**
     * An annotation processing tool framework
     */
    private ProcessingEnvironment processingEnv;

    /**
     * 初始化处理器
     *
     * @param processingEnv 提供了一系列的实用工具
     */
    @SneakyThrows
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.javacTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        System.out.println("WheelVersionProcessor init finish");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 遍历所有的注解
        // 这里只有 WheelVersion
        for (TypeElement t : annotations) {
            // 获取到给定注解的 element（element可以是一个类、方法、包等）
            // 这里一定是变量
            for (Element e : roundEnv.getElementsAnnotatedWith(t)) {
                // JCVariableDecl为字段或者变量定义语法树节点
                // 这里一定是变量
                JCTree.JCVariableDecl jcv = (JCTree.JCVariableDecl) javacTrees.getTree(e);
                Type varType = jcv.vartype.type;
                // 限定变量类型必须是String类型
                System.out.println("The type of var is " + varType.toString());
                if (!"java.lang.String".equals(varType.toString())) {
                    printErrorMessage(e, "Type '" + varType + "'" + " is not support.");
                }
                // 给这个字段赋值，也就是getVersion的返回值

                jcv.init = treeMaker.Literal(getVersion());
            }
        }
        return true;
    }

    /**
     * 利用 processingEnv 内的 Messager 对象输出一些日志
     *
     * @param e element
     * @param m error message
     */
    private void printErrorMessage(Element e, String m) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, m, e);
    }

    private String getVersion() {
        // 获取version，这里省略掉复杂的代码，直接返回固定值
        return VERSION;
    }

}
