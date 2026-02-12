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
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation(libs.micronaut.session)
    implementation(libs.micronaut.validation)
    implementation(libs.jakarta.validation)
    annotationProcessor(libs.micronaut.validation)
    
    runtimeOnly(libs.logback.classic)
    runtimeOnly("org.yaml:snakeyaml")
}

application {
    mainClass.set("dev.ayam.folderinspector.launcher.web.WebLauncher")
}

// Note: Frontend build is done manually with:
//   cd frontend && bun run build
//   cp -r frontend/.output/public/* src/main/resources/public/
