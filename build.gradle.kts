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
    if (name != "bom") {
        apply(plugin = "java-library")

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
