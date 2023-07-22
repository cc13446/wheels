package com.cc.wheel.dubbo.extension

import com.cc.wheel.dubbo.common.URL
import com.cc.wheel.dubbo.compiler.Compiler
import com.cc.wheel.dubbo.compiler.support.AdaptiveCompiler
import spock.lang.Specification

/**
 * @author cc
 * @date 2023/7/21
 */
class ExtensionLoaderTest extends Specification {

    def "base test" () {
        when:
        Compiler res = ExtensionLoader.getExtensionLoader(Compiler).getAdaptiveExtension()

        then:
        res.class == AdaptiveCompiler
    }

    def "extension test" () {
        given:
        TestExtension adaptive = ExtensionLoader.getExtensionLoader(TestExtension).getAdaptiveExtension()
        URL helloUrl = new URL(["key" : "hello"])
        URL worldUrl = new URL(["key" : "world"])

        when:
        String hello = adaptive.test(helloUrl)
        String world = adaptive.test(worldUrl)

        then:
        hello == "hello"
        world == "world"
    }
}
