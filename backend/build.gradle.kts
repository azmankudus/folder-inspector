plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.shadow)
    alias(libs.plugins.micronaut.aot)
}

version = "0.1"
group = "backend"

repositories {
    mavenCentral()
    maven { url = uri("https://build.shibboleth.net/nexus/content/repositories/releases/") }
}

dependencies {
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.data.processor)
    annotationProcessor(libs.micronaut.security.annotations)
    
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.data.jdbc)
    implementation(libs.micronaut.jdbc.hikari)
    implementation(libs.micronaut.cache.caffeine)
    implementation(libs.quartz)
    implementation(libs.micronaut.security)
    implementation(libs.micronaut.security.jwt)
    implementation(libs.micronaut.security.oauth2)
    implementation(libs.pac4j.saml)
    implementation(libs.pac4j.core)
    
    implementation(libs.smbj)
    implementation(libs.reactor.core)
    implementation(libs.poi.ooxml)
    implementation(libs.opencsv)
    implementation(libs.micronaut.liquibase)
    
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.postgresql)
    
    compileOnly(libs.micronaut.http.client)
    testImplementation(libs.micronaut.http.client)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "backend.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}


graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("backend.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}


