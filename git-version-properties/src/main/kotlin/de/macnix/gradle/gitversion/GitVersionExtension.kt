package de.macnix.gradle.gitversion

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

interface GitVersionInterfaceExtension {
    val applicationName: Property<String>
    val versionMajor: Property<String>
    val versionMinor: Property<String>
    val versionPatch: Property<String>
    val buildNumber: Property<String>
    val buildDate: Property<String>
    val gitBranch: Property<String>
    val gitSha: Property<String>
    val extraProperties: MapProperty<String, String>
    val applyVersionPropertiesAfterFileCreation: Property<Boolean>
}
