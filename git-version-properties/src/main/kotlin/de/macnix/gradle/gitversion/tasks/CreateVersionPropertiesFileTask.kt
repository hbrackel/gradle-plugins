package de.macnix.gradle.gitversion.tasks

import de.macnix.gradle.gitversion.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CreateVersionPropertiesFileTask : DefaultTask() {
    init {
        group = GitVersionPropertiesPlugin.TASK_GROUP
        description = "create version.properties file"
    }

    @get:Input
    abstract val applicationName: Property<String>

    @get:Input
    abstract val versionMajor: Property<String>

    @get:Input
    abstract val versionMinor: Property<String>

    @get:Input
    abstract val versionPatch: Property<String>

    @get:Input
    abstract val buildNumber: Property<String>

    @get:Input
    abstract val buildDate: Property<String>

    @get:Input
    abstract val gitBranch: Property<String>

    @get:Input
    abstract val gitSha: Property<String>

    @get:Input
    abstract val extraProperties: MapProperty<String, String>

    @get:Input
    abstract val applyVersionPropertiesAfterFileCreation: Property<Boolean>

    @get:OutputFile
    abstract val versionPropertiesFile: RegularFileProperty

    @TaskAction
    fun action() {
        val versionProperties = VersionProperties(
            applicationName = applicationName.get(),
            versionMajor = versionMajor.get(),
            versionMinor = versionMinor.get(),
            versionPatch = versionPatch.get(),
            buildNumber = buildNumber.get(),
            buildDate = buildDate.get(),
            gitSha = gitSha.get(),
            gitBranch = gitBranch.get(),
            extraProperties = extraProperties.get(),
        )
        versionProperties.toVersionPropertiesFile(versionPropertiesFile.get().asFile)
        if (applyVersionPropertiesAfterFileCreation.get()) {
            project.setProjectVersion(versionProperties)
            project.setGradleExtraProperty(versionProperties)
        }
    }
}
