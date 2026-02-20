plugins {
    `java-library`
}

dependencies {
    implementation(project(":core"))
    implementation(rootProject.libs.hibernate.core)
    implementation(rootProject.libs.hibernate.community.dialects)
    implementation(rootProject.libs.jakarta.persistence)
    // Using H2 for JPA demonstration or SQLite can be used too. 
    // Let's use SQLite for consistency if user didn't specify.
    implementation(rootProject.libs.sqlite.jdbc)
}
