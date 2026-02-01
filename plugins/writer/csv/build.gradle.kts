dependencies {
    implementation(platform(project(":bom")))
    api(project(":core"))
    implementation(rootProject.libs.jackson.dataformat.csv)
}
