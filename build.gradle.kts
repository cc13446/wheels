import org.gradle.api.tasks.testing.logging.TestLogEvent

subprojects {
    apply(plugin = "java")
    apply(plugin = "groovy")
    apply(plugin = "application")

    // 配置仓库
    repositories {
        mavenCentral()
    }

    // 配置java版本
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 配置版本
    val groovyVersion = "4.0.5"
    val spockVersion = "2.3-groovy-4.0"

    // 公共依赖
    dependencies {
        "implementation"(platform("org.apache.groovy:groovy-bom:$groovyVersion"))
        "implementation"("org.apache.groovy:groovy")

        "implementation"(platform("org.spockframework:spock-bom:$spockVersion"))
        "testImplementation"("org.spockframework:spock-core")
        "testImplementation"("org.spockframework:spock-junit4")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            this.showStandardStreams = true
        }
    }
}