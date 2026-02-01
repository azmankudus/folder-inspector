plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(rootProject.libs.picocli)
        api(rootProject.libs.slf4j.api)
        api(rootProject.libs.logback.classic)
        api(rootProject.libs.testng)
    }
}
