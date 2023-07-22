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
    val apacheCommonsLangVersion = "3.12.0"
    val apacheCommonsCollectionsVersion = "4.4"
    val jacksonDatabindVersion = "2.15.2"
    val lombokVersion = "1.18.28"
    val slf4jVersion = "2.0.7"

    // 公共依赖
    dependencies {
        // groovy
        "implementation"(platform("org.apache.groovy:groovy-bom:$groovyVersion"))
        "implementation"("org.apache.groovy:groovy")

        // spock
        "implementation"(platform("org.spockframework:spock-bom:$spockVersion"))
        "testImplementation"("org.spockframework:spock-core")
        "testImplementation"("org.spockframework:spock-junit4")

        // apache utils
        "implementation"("org.apache.commons:commons-lang3:$apacheCommonsLangVersion")
        "implementation"("org.apache.commons:commons-collections4:$apacheCommonsCollectionsVersion")

        // jackson-databind
        "implementation"("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")

        // lombok
        "compileOnly"("org.projectlombok:lombok:$lombokVersion")
        "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")

        "testCompileOnly"("org.projectlombok:lombok:$lombokVersion")
        "testAnnotationProcessor"("org.projectlombok:lombok:$lombokVersion")

        // slf4j
        "implementation"("org.slf4j:slf4j-simple:$slf4jVersion")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            this.showStandardStreams = true
        }
    }
}