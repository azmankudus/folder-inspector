plugins {
    `java-library`
}

dependencies {
    implementation(platform(project(":bom")))
    implementation(project(":core"))
    
    // Launcher implementation needs core interfaces
}

