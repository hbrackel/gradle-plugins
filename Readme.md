Git-Version-Properties Gradle Plugin
====================================

Description
-----------

This plugin helps automating the project versioning in a CI/CD environment by
combining the latest Git tag with a CI/CD build number for composing the project version number.
Together with additional configurable properties the version components can be persisted
in a `gitVersionProperties/version.properties` file in the project `build` directory, as defined by the implicitly
applied Gradle `base` plugin.

The project is assumed to use [Semantic Versioning](https://semver.org), but other schemes are possible as well.

The following VersionProperties are supported:

| Property          | Default Value                                                                               | Description                                                                                                                         |
|-------------------|---------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `applicationName` | (`applicationName` from version.properties) ?: `project.name`                               | name of the application/project                                                                                                     |
| `versionMajor`    | (`versionMajor` from version.properties) ?: (derived from git tag - see below)              | "MAJOR" version component                                                                                                           |
| `versionMinor`    | (`versionMinor` from version.properties) ?: (derived from git tag - see below)              | "MINOR" version component                                                                                                           |
| `versionPatch`    | (`versionPatch` from version.properties) ?: (derived from git tag - see below)              | "PATCH" version component                                                                                                           |
| `gitBranch`       | (`gitBranch` from version.properties) ?: (derived from git - see below)                     | the current git branch                                                                                                              |
| `gitSha`          | (`gitSha` from version.properties) ?: (derived from git - see below)                        | short git hash                                                                                                                      |
| `buildNumber`     | (`buildNumber` from version.properties) ?: `System.getenv("BUILD_NUMBER")?:"0"              | the build number from the CI/CD system                                                                                              |
| `buildDate`       | (`buildDate` from version.properties) ?: `ZonedDateTime.now().format(DateTime.ISO_INSTANT)` | the date of the build                                                                                                               |
| `version`         | `$versionMajor.$versionMinor.%versionPatch`                                                 | computed full version                                                                                                               |
| `versionComplete` | `$version-$buildNumber`                                                                     | computed version incl. build number                                                                                                 |
| `extraProperties` | (`extraProperties` from version.properties) ?: `emptyMap<String, String>()`                 | additional custom properties configured in the `gitVersionProperties` extensions after the version.properties file has been created 

All properties are evaluated immediately after the plugin has been applied, so that they can already be used in the
configuration phase of project tasks. As a consequence, the properties defined in the `gitVersionProperties`
extension block only become effective after the `version.properties` file as been updated by the respective task (see
notes below).

**Git**

This plugin expects git 2.22 or later be installed on the system.

`versionMajor`, `versionMinor`, `versionPatch`, and the Git `sha` are derived from the result of the
command `git describe --tags --long --always`.

If the latest Git tag contains a dot (`<first part>.<second minor>`), the tag string is split around the first dot,
where the first part is taken as the `MAJOR`version component and the second part comprises the `MINOR` version
component.
`PATCH` is calculated from the number of commits since the last tag.
The principles of semantic versioning are fulfilled when tags are formatted using numbers like `2.3`.

If no tag can be found, `MAJOR.MINOR.PATCH` defaults to `0.0.0`

The Git branch name is derived from the command `git branch --show-current`. If no branch can be
found, `<unknown branch>` is set as the default value.

In a CI/CD multi-branch configuration the version will be postfixed with `".SNAPSHOT.${buildDate.toEpochSecond()}"` for
all branches, which are not named `master`, `main`, or whose name do not end with `-release`.

Most of the above behavior can be overridden by specifying the desired properties in the `gitVersionProperties`extension
block.

Usage
-----

The plugin is applied using the standard Gradle options. The `git-version-properties` plugin also applies the
Gradle `base` plugin and inherits its related tasks.

**Kotlin DSL**

```kotlin
plugins {
    id("de.macnix.gradle.git-version-properties") version "1.1.0"
}
```

As mentioned above, the project version as well as the extra property `versionProperties` are set immediately after the
plugin has been applied and before the extension has been configured.
Setting the boolean extension property `applyVersionPropertiesAfterFileCreation` to true (default), the project version
and extra property are set with the fully evaluated values after the `version.properties` has been created.

**Note:**

```
gitVersionProperties {
    applyVersionPropertiesAfterFileCreation = true
}
```

may lead to different values for the `project.version` and the `applyVersionPropertiesAfterFileCreation` extra
properties between the configuration and execution phases, if the configuration of the gitVersionProperties extension
leads to changes in the `version.properties` file.

_Example: Difference between configuration and execution properties_

```
plugins {
    id("de.macnix.gradle.git-version-properties") version "1.1.0"
}

// [...]

gitVersionProperties {
    applicationName = "configuredAppName"
    versionMajor = "xyz"
    applyVersionPropertiesAfterFileCreation = true  // default
    extraProperties.set(
        mapOf(
            "dockerImagePrefix" to "my-repo",
            "specialProp" to "432"
        )
    )
}

tasks.create("showVersionProperties") {
    dependsOn("createVersionPropertiesFile")
    println("--- configuration phase ---")
    println("project.version: ${project.version}")
    println((project.extra.get("versionProperties") as VersionProperties).toFormattedString())
    doLast {
        println("--- execution phase ---")
        println("project.version: ${project.version}")
        println((project.extra.get("versionProperties") as VersionProperties).toFormattedString())
    }
}

```

_Output of task `showVersionProperties`_ on a clean project

```
> Configure project :
--- configuration phase ---
project.version: 3.2.0
------------------------------------------------
versionProperties:
  applicationName      -> plugin-consumer
  versionMajor         -> 3
  versionMinor         -> 2
  versionPatch         -> 0
  version              -> 3.2.0
  versionComplete      -> 3.2.0-0
  buildNumber          -> 0
  buildDate            -> 2024-01-09T14:59:11.802220Z
  gitBranch            -> master
  gitSha               -> 8d0a6d2
  extraProperties:     ->
------------------------------------------------


> Task :createVersionPropertiesFile

> Task :showVersionProperties
--- execution phase ---
project.version: xyz.2.0
------------------------------------------------
versionProperties:
  applicationName      -> configuredAppName
  versionMajor         -> xyz
  versionMinor         -> 2
  versionPatch         -> 0
  version              -> xyz.2.0
  versionComplete      -> xyz.2.0-0
  buildNumber          -> 0
  buildDate            -> 2024-01-09T14:59:11.802220Z
  gitBranch            -> master
  gitSha               -> 8d0a6d2
  extraProperties:     ->
    dockerImagePrefix  -> my-repo
    specialProp        -> 432
------------------------------------------------
```

Configuration
-------------

The plugin adds the `gitVersionProperties`extension. All default version properties can be overridden
using the Gradle extension DSL and extra properties can be added.

_Example_:

**Kotlin DSL**

```kotlin
gitVersionProperties {
    versionMajor = "1"
    buildNumber = getenv("BUILD_ID") ?: ""
    extraProperties = mapOf(
        "dockerImagePrefix" to "my-prefix",
    )
}
```

Tasks
-----

This plugin adds the following tasks to the project to the `gitVersionProperties` group (in addition to the tasks of
the `base` plugin):

| Task                          | Description                                                                                  |
|-------------------------------|----------------------------------------------------------------------------------------------|
| `createVersionPropertiesFile` | create the `version.properties` file in `<buildDir>/gitVersionProperties/version.properties` |
| `deleteVersionPropertiesFile` | delete the `version.properties` file in `<buildDir>/gitVersionProperties/version.properties` |
| `printVersionProperties`      | print effective version properties                                                           |

License
-------
This plugin is made available under the Apache 2.0 License.

Personal Note
-------------
This is the first Gradle plugin I developed. Please forgive any non-ideomatic or inefficient approaches or code
sections.
Tests / FunctionalTests are still missing.
Any constructive feedback will be much appreciated.