rootProject.name = "folder-inspector"

include("bom")
include("core")


// Plugins
include("plugins:launcher:console")
include("plugins:launcher:rest")
include("plugins:launcher:web")
include("plugins:scanner:local")
include("plugins:formatter:text")
include("plugins:formatter:csv")
include("plugins:writer:console")
include("plugins:writer:file")
include("plugins:notifier:console")

// Fix project name collisions (renaming changes the project path!)
project(":plugins:launcher:console").name = "launcher-console"
project(":plugins:scanner:local").name = "scanner-local"
project(":plugins:formatter:text").name = "formatter-text"
project(":plugins:formatter:csv").name = "formatter-csv"
project(":plugins:launcher:rest").name = "launcher-rest" // Added for RestLauncher
project(":plugins:launcher:web").name = "launcher-web" // Added for WebLauncher
project(":plugins:writer:console").name = "writer-console"
project(":plugins:writer:file").name = "writer-file"
project(":plugins:notifier:console").name = "notifier-console"
