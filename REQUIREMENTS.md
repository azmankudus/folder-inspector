# Folder Inspector

|||
|---|---|
|Application type|Terminal CLI|
|Java version|25|
|Build tool|Gradle|
|Testing framework|TestNG|
|Static code analysis tool|SonarQube|
|Version control|Git|
|License|MIT|

## Package structure

### BOM

|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector|bom|Bill of Materials|

### Core
|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector|core|Contains main, core logic, model and interfaces|

### Plugins 

#### Launcher
|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector.launcher|cli|CLI implementation|

#### Scanner
|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector.scanner|local|Local file system scanner|

#### Writer
|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector.writer|console|Console writer|
|dev.ayam.folderinspector.writer|csv|CSV writer|

#### Notifier
|group|name|purpose|
|---|---|---|
|dev.ayam.folderinspector.notifier|console|Console notifier|

## Scope
1. Target platfomr
    1. Linux
    2. (not yet) Windows
    3. (not yet) SMB/CIFS
    4. (not yet) NFS
    5. (not yet) S3
2. Output format
    1. Console
    2. CSV
    3. (not yet) Excel
    4. (not yet) HTML
    5. (not yet) PDF
    6. (not yet) JSON
    7. (not yet) XML
    8. (not yet) YAML

## Input parameters:
1. Folder path (default: current directory)
2. Output format (default: console)

## Output:
1. Table of
    1. Folder path (absolute)
    2. File name (relative to folder path)
    3. Item type (File, Directory, Symbolic link, etc.)
    4. Size (file only)
    5. Last modified time (yyyy-MM-dd HH:mm:ss ZZZZ)
    6. Created time (yyyy-MM-dd HH:mm:ss ZZZZ)
    7. Owner (user name)
    8. Group (group name)
    9. Basic permissions (rwxrwxrwx)
    10. ACLs
        1. ACL type (user, group, other)
        2. ACL owner (user name)
        3. ACL permissions (rwxrwxrwx)

## Error codes and exit codes
|Error code|Exit code|Description|
|---|---|---|
|1|1|Invalid folder path|
|2|2|Invalid output format|
|3|3|Insufficient permissions|
|4|4|File not found|
|5|5|Directory not found|
|6|6|Symbolic link not found|
|7|7|Other errors|

## Libraries
|Name|Version|License|
|---|---|---|
|PicoCLI||