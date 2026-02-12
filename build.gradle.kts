plugins {
    alias(libs.plugins.sonarqube)
}

allprojects {
    group = "dev.ayam.folderinspector"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    val isRealProject = file("build.gradle.kts").exists()
    if (name != "bom" && isRealProject) {
        apply(plugin = "java-library")
        apply(plugin = "eclipse")

        plugins.withId("java-library") {
            configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }

            dependencies {
                "api"(rootProject.libs.slf4j.api) 
                "implementation"(rootProject.libs.logback.classic)
                "testImplementation"(rootProject.libs.testng)
            }

            tasks.named<Test>("test") {
                useTestNG()
            }
        }


    }
}
