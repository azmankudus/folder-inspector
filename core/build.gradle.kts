plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(platform(project(":bom")))
    api(rootProject.libs.picocli)
    implementation(rootProject.libs.slf4j.api)
    testImplementation(rootProject.libs.testng)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "dev.ayam.folderinspector.core.Main"
}

tasks.named<Test>("test") {
    // Use TestNG for unit tests.
    useTestNG()
}

// Add plugins to the classpath of the run task
tasks.named<JavaExec>("run") {
    val pluginPaths = listOf(
        ":launcher-console",
        ":launcher-rest",
        ":launcher-web",
        ":scanner-local",
        ":scanner-dummy",
        ":formatter-text",
        ":formatter-csv",
        ":writer-console",
        ":writer-file",
        ":notifier-console",
        ":database-jpa",
        ":scheduler-quartz"
    )

    val pluginProjects = pluginPaths.map { project(it) }

    // Ensure plugin jars are built before running
    dependsOn(pluginProjects.map { it.tasks.named("jar") })

    // Add plugin runtime classpaths (includes dependencies like Jackson)
    classpath += files(pluginProjects.map { 
        it.extensions.getByType(SourceSetContainer::class).getByName("main").runtimeClasspath 
    })
}
