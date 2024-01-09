package de.macnix.gradle.gitversion

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test

class GitVersionPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test
    fun `can run task`() {
        settingsFile.writeText("")
        buildFile.writeText(
            """
            plugins {
                id('de.macnix.gradle.gitversion')
            }
        """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("createVersionProperties")
        runner.withProjectDir(projectDir)
//        val result = runner.build()

    }
}
