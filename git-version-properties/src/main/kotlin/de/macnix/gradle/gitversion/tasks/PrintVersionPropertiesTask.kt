package de.macnix.gradle.gitversion.tasks

import de.macnix.gradle.gitversion.GitVersionPropertiesPlugin
import de.macnix.gradle.gitversion.VersionProperties
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PrintVersionPropertiesTask : DefaultTask() {
    init {
        group = GitVersionPropertiesPlugin.TASK_GROUP
        description = "print effective versionProperties"
    }

    @get:Input
    abstract val versionProperties: Property<VersionProperties>

    @TaskAction
    fun action() {
        val msg = buildString {
            println(versionProperties.get().toFormattedString())
        }
        println(msg)
    }
}
