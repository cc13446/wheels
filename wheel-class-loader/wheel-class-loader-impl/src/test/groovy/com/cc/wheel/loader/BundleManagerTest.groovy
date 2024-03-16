package com.cc.wheel.loader


import spock.lang.Specification
/**
 * @author cc
 * @date 2024/3/16
 */
class BundleManagerTest extends Specification{

    def "test bundle loader" () {
        given:
        // 构建demo模块jar包
        def processBuilder = new ProcessBuilder("bash", "-c", "gradle jar")
        processBuilder.directory(new File("../wheel-class-loader-demo"))
        def process = processBuilder.start()
        def reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        def line
        while (Objects.nonNull(line = reader.readLine())) {
            System.out.println(line)
        }
        process.waitFor()

        // 准备测试环境
        def id = "demo"
        def otherId = "demo_other"
        def target = new BundleManager()
        target.loadBundle("../wheel-class-loader-demo/build/libs/wheel-class-loader-demo.jar", id)
        def bundle = target.getBundle(id)
        target.loadBundle("../wheel-class-loader-demo/build/libs/wheel-class-loader-demo.jar", otherId)
        def otherBundle = target.getBundle(otherId)

        when:
        bundle.event(["demo":"test"])
        otherBundle.event(["demo_other":"test"])

        then:
        bundle.getClass().getClassLoader() != otherBundle.getClass().getClassLoader()

        when:
        target.destroyBundle(id)
        target.destroyBundle(otherId)

        then:
        Objects.isNull(target.getBundle(id))
    }
}
