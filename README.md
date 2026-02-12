# Folder Inspector

A powerful Java-based terminal CLI tool for inspecting folder contents, permissions, and metadata.

## Overview

Folder Inspector allows you to scan local directories and output detailed information including paths, file names, types, sizes, permissions, and ACLs in various formats like Console and CSV.

## Tech Stack

- **Java**: 25
- **Build Tool**: Gradle
- **CLI Framework**: PicoCLI
- **Testing**: TestNG
- **Analysis**: SonarQube

## Project Structure

- `core`: Core logic, models, and interfaces.
- `bom`: Bill of Materials for dependency management.
- `launcher-*`: Various entry points (Console, REST, Web).
- `scanner-*`: File system scanning implementations (Local, Dummy).
- `formatter-*`: Output formatting logic (CSV, Text).
- `writer-*`: Output writing implementations (Console, File).
- `notifier-*`: User notification modules.

## Getting Started

### Prerequisites
- JDK 25 or higher
- Gradle

### Build
```bash
./gradlew build
```

### Run (Console Launcher)
```bash
./gradlew :launcher-console:run --args="/path/to/inspect --format=console"
```

## License
MIT License - see [LICENSE](LICENSE) for details.
