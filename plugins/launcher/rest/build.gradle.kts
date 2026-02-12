plugins {
    id("io.micronaut.application") version "4.3.4"
    id("io.micronaut.aot") version "4.3.4"
}

micronaut {
    version("4.10.7")
}

dependencies {
    implementation(platform(project(":bom")))
    implementation(project(":core"))
    
    implementation(libs.micronaut.http.server.netty)
    implementation(libs.micronaut.jackson.databind)
    implementation(libs.micronaut.security)
    implementation(libs.micronaut.security.jwt)
    implementation(libs.micronaut.validation)
    implementation(libs.jakarta.validation)
    
    annotationProcessor(libs.micronaut.security.annotations)
    
    implementation("jakarta.annotation:jakarta.annotation-api")
    
    runtimeOnly(libs.logback.classic)
    runtimeOnly("org.yaml:snakeyaml")
}

application {
    mainClass.set("dev.ayam.folderinspector.launcher.rest.RestLauncher")
}
