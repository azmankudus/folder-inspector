---
description: Integration details for utilizing localized testing environments
---
# Tooling and Environment Commands

An agent should exclusively execute the following scripts when attempting to compile, boot, or validate application boundaries:

## Backend Build Commands
Validate logic without running tests: `./gradlew build -x test`.
Start REST instance on localhost 8080: `./gradlew run`.

## Frontend Build Commands
Run Vite local development node: `npm run dev`.

Do NOT attempt to run standard Node testing architectures or Java spring-boot compilation models.
