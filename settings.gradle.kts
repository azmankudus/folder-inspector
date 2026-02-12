plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "folder-inspector"

include("bom")
include("core")
include("scheduler-quartz")


// Plugins
include("launcher-console")
include("launcher-rest")
include("launcher-web")
include("scanner-local")
include("scanner-dummy")
include("formatter-text")
include("formatter-csv")
include("writer-console")
include("writer-file")
include("notifier-console")
include("database-jpa")
