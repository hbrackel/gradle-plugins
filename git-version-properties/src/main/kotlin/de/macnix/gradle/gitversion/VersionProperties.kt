package de.macnix.gradle.gitversion

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

data class VersionProperties(
    val applicationName: String,
    val versionMajor: String,
    val versionMinor: String,
    val versionPatch: String,
    val buildNumber: String,
    val buildDate: String,
    val gitSha: String,
    val gitBranch: String,
    val extraProperties: Map<String, String>
) {
    companion object

    val version: String = "$versionMajor.$versionMinor.$versionPatch"
    val versionComplete: String = "$version-$buildNumber"

    fun toFormattedString(): String {
        return buildString {
            this.append("-".repeat(48))
            this.appendLine()
            this.appendLine("versionProperties:")
            this.appendLine(String.format("  %-20s -> %s", "applicationName", applicationName))
            this.appendLine(String.format("  %-20s -> %s", "versionMajor", versionMajor))
            this.appendLine(String.format("  %-20s -> %s", "versionMinor", versionMinor))
            this.appendLine(String.format("  %-20s -> %s", "versionPatch", versionPatch))
            this.appendLine(String.format("  %-20s -> %s", "version", version))
            this.appendLine(String.format("  %-20s -> %s", "versionComplete", versionComplete))
            this.appendLine(String.format("  %-20s -> %s", "buildNumber", buildNumber))
            this.appendLine(String.format("  %-20s -> %s", "buildDate", buildDate))
            this.appendLine(String.format("  %-20s -> %s", "gitBranch", gitBranch))
            this.appendLine(String.format("  %-20s -> %s", "gitSha", gitSha))
            this.appendLine(String.format("  %-20s ->", "extraProperties:"))
            extraProperties.forEach { (key, value) ->
                this.appendLine(String.format("    %-18s -> %s", key, value))
            }
            this.appendLine("-".repeat(48))
        }
    }
}

fun VersionProperties.Companion.from(props: Properties): VersionProperties {
    return VersionProperties(
        applicationName = props["applicationName"]?.toString() ?: "",
        gitSha = props["gitSha"]?.toString() ?: "",
        gitBranch = props["gitBranch"]?.toString() ?: "",
        buildNumber = props["buildNumber"]?.toString() ?: "",
        buildDate = props["buildDate"]?.toString() ?: "",
        versionMajor = props["versionMajor"]?.toString() ?: "",
        versionMinor = props["versionMinor"]?.toString() ?: "",
        versionPatch = props["versionPatch"]?.toString() ?: "",
        extraProperties = props.stringPropertyNames()
            .filter { it.startsWith("extraProperties.") }.associate {
                Pair(it.removePrefix("extraProperties."), props[it] as? String ?: "")
            }
    )
}

fun VersionProperties.toProperties(): Properties {
    val props = Properties()
    props["applicationName"] = applicationName
    props["versionMajor"] = versionMajor
    props["versionMinor"] = versionMinor
    props["versionPatch"] = versionPatch
    props["buildNumber"] = buildNumber
    props["buildDate"] = buildDate
    props["gitSha"] = gitSha
    props["gitBranch"] = gitBranch
    props["version"] = version
    props["versionComplete"] = versionComplete
    extraProperties.forEach { (key, value) ->
        props["extraProperties.$key"] = value
    }
    return props
}

fun VersionProperties.Companion.fromVersionPropertiesFile(path: String): Optional<VersionProperties> {
    val propsFile = File(path)
    return if (propsFile.exists()) {
        Optional.of(VersionProperties.from(Properties().apply {
            load(FileInputStream(propsFile))
        }))
    } else {
        Optional.empty<VersionProperties>()
    }
}

fun VersionProperties.toVersionPropertiesFile(file: File) {
    this.toProperties().store(FileOutputStream(file), "")
}
