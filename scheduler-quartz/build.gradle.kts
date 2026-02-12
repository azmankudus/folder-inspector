plugins {
    id("java-library")
}

dependencies {
    implementation(project(":core"))
    api(rootProject.libs.quartz)
    implementation(libs.slf4j.api)
}
