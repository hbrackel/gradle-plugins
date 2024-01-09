package de.macnix.gradle.gitversion

import de.macnix.gradle.gitversion.GitVersionPropertiesPlugin.Companion.VERSION_PROPERTIES_PATH
import de.macnix.gradle.gitversion.tasks.CreateVersionPropertiesFileTask
import de.macnix.gradle.gitversion.tasks.PrintVersionPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.BasePlugin
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GitVersionPropertiesPlugin : Plugin<Project> {
    companion object {
        const val TASK_GROUP = "gitVersionProperties"
        const val VERSION_PROPERTIES_PATH = "gitVersionProperties/version.properties"
    }

    override fun apply(project: Project) {
        project.run {
            pluginManager.apply(BasePlugin::class.java)
            val extension = extensions.create("gitVersionProperties", GitVersionInterfaceExtension::class.java)
            val versionProperties = readVersionPropertiesFile()
                .map {
                    logger.log(
                        LogLevel.INFO,
                        "{} - setting default values from version.properties file",
                        GitVersionPropertiesPlugin::class.java.simpleName
                    )
                    it
                }
                .orElseGet {
                    logger.log(
                        LogLevel.INFO,
                        "{} - setting default values from gitVersion and buildInfo",
                        GitVersionPropertiesPlugin::class.java.simpleName
                    )
                    val gitVersion = gitVersion()
                    val buildInfo = buildInfo()
                    val effectiveVersion = effectiveVersion(gitVersion, buildInfo.second)
                    VersionProperties(
                        applicationName = name,
                        versionMajor = effectiveVersion.first,
                        versionMinor = effectiveVersion.second,
                        versionPatch = effectiveVersion.third,
                        buildNumber = buildInfo.first,
                        buildDate = buildInfo.second.format(DateTimeFormatter.ISO_INSTANT),
                        gitSha = gitVersion.sha,
                        gitBranch = gitVersion.branch,
                        extraProperties = emptyMap()
                    )
                }
            setProjectVersion(versionProperties)
            setGradleExtraProperty(versionProperties)
            extension.setExtensionDefaults(versionProperties)

            tasks.register("printVersionProperties", PrintVersionPropertiesTask::class.java) {
                it.versionProperties.set(project.extensions.extraProperties["versionProperties"] as VersionProperties)
            }

            tasks.register("createVersionPropertiesFile", CreateVersionPropertiesFileTask::class.java) {
                it.applicationName.set(extension.applicationName)
                it.versionMajor.set(extension.versionMajor)
                it.versionMinor.set(extension.versionMinor)
                it.versionPatch.set(extension.versionPatch)
                it.buildNumber.set(extension.buildNumber)
                it.buildDate.set(extension.buildDate)
                it.gitBranch.set(extension.gitBranch)
                it.gitSha.set(extension.gitSha)
                it.extraProperties.set(extension.extraProperties)
                it.versionPropertiesFile.set(project.layout.buildDirectory.file(VERSION_PROPERTIES_PATH))
                it.applyVersionPropertiesAfterFileCreation.set(extension.applyVersionPropertiesAfterFileCreation)
            }

            tasks.create("deleteVersionPropertiesFile")
            {
                it.group = TASK_GROUP
                it.description = "delete version.properties file and parent directory"
                it.doLast {
                    val versionFile = project.layout.buildDirectory.file(VERSION_PROPERTIES_PATH).get().asFile
                    if (versionFile.parentFile.exists()) {
                        versionFile.parentFile.deleteRecursively()
                    }
                }
            }
        }
    }
}

private fun readVersionPropertiesFile(): Optional<VersionProperties> {
    val versionFile = File("build/$VERSION_PROPERTIES_PATH")
    return if (versionFile.exists()) {
        VersionProperties.fromVersionPropertiesFile(versionFile.path)
    } else {
        Optional.empty()
    }
}

private fun GitVersionInterfaceExtension.setExtensionDefaults(versionProps: VersionProperties) {
    with(this) {
        applicationName.convention(versionProps.applicationName)
        versionMajor.convention(versionProps.versionMajor)
        versionMinor.convention(versionProps.versionMinor)
        versionPatch.convention(versionProps.versionPatch)
        buildNumber.convention(versionProps.buildNumber)
        buildDate.convention(versionProps.buildDate)
        gitSha.convention(versionProps.gitSha)
        gitBranch.convention(versionProps.gitBranch)
        extraProperties.convention(emptyMap())
        applyVersionPropertiesAfterFileCreation.convention(true)
    }
}

internal fun Project.setProjectVersion(versionProperties: VersionProperties) {
    with(versionProperties) {
        project.version = "$versionMajor.$versionMinor.$versionPatch"
    }
}

internal fun Project.setGradleExtraProperty(versionProperties: VersionProperties) {
    project.extensions.extraProperties.set("versionProperties", versionProperties)
}

internal fun effectiveVersion(
    gitVersion: GitVersion,
    buildDate: ZonedDateTime
): Triple<String, String, String> {
    val versionSuffix =
        if (gitVersion.branch !in listOf("main", "master") && !gitVersion.branch.endsWith("-release")) {
            ".SNAPSHOT.${buildDate.toEpochSecond()}"
        } else {
            ""
        }
    val split = gitVersion.version.split(".")
    return when {
        split.isEmpty() -> Triple("0", "0", "0$versionSuffix")
        split.size == 1 -> Triple(split[0], "0", "0$versionSuffix")
        split.size == 2 -> Triple(split[0], split[1], "0$versionSuffix")
        else -> Triple(split[0], split[1], split.subList(2, split.size).joinToString(".") + versionSuffix)
    }
}