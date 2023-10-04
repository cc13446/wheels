dependencies {
    compileOnly(project(mapOf("path" to ":wheel-lombok-annotation:wheel-annotation")))
    annotationProcessor(project(mapOf("path" to ":wheel-lombok-annotation:wheel-annotation-processor")))
    implementation(project(mapOf("path" to ":wheel-lombok-annotation:wheel-annotation-processor")))
}