rootProject.name = "folder-inspector"

include("bom")
include("core")


// Plugins
include("plugins:launcher:console")
include("plugins:scanner:local")
include("plugins:writer:console")
include("plugins:writer:csv")
include("plugins:notifier:console")

// Fix project name collisions (renaming changes the project path!)
project(":plugins:launcher:console").name = "launcher-console"
project(":plugins:scanner:local").name = "scanner-local"
project(":plugins:writer:console").name = "writer-console"
project(":plugins:writer:csv").name = "writer-csv"
project(":plugins:notifier:console").name = "notifier-console"

