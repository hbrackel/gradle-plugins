package de.macnix.gradle.gitversion

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.io.File
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitVersionPropertiesPluginFunctionalTest {

    private lateinit var projectDir: File
    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

    private fun versionPropertiesFile(): File {
        return File(projectDir, "build/gitVersionProperties/version.properties")
    }

    @BeforeAll
    fun beforeAll(@TempDir projectDir: File) {
        this.projectDir = projectDir
        settingsFile.writeText("")
        buildFile.writeText(
            """
            plugins {
                id("de.macnix.gradle.git-version-properties")
                `java`
            }
            
            gitVersionProperties {
                applicationName = "my-app"
                versionMajor = "1"
                versionMinor = "2"
                versionPatch = "3"
                buildNumber = "4"
                buildDate = "2024-01-10T01:02:03.456Z"
                extraProperties = mapOf(
                    "extraProp1" to "extraProp1.value"
                )
            }
        """.trimIndent()
        )
    }

    @Nested
    @DisplayName("printVersionProperties task")
    inner class PrintVersionPropertiesTask {
        @Test
        fun `can be run`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("printVersionProperties")
            runner.withProjectDir(projectDir)
            val output = runner.build().output

            expectThat(output) {
                contains("> Task :printVersionProperties")
            }
        }

        @Test
        fun `prints the configured properties`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("printVersionProperties")
            runner.withProjectDir(projectDir)
            val output = runner.build().output

            expectThat(output) {
                contains("------------------------------------------------")
                contains("versionProperties:")
                contains("applicationName      -> ")
                contains("versionMajor         -> ")
                contains("versionMinor         -> ")
                contains("versionPatch         -> ")
                contains("version              -> ")
                contains("versionComplete      -> ")
                contains("buildNumber          -> ")
                contains("buildDate            -> ")
                contains("gitBranch            -> ")
                contains("gitSha               -> ")
                contains("extraProperties:     ->")

            }
        }
    }

    @Nested
    @DisplayName("createVersionPropertiesFile task")
    inner class CreateVersionPropertiesFileTask {
        @Test
        fun `can be run`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("createVersionPropertiesFile")
            runner.withProjectDir(projectDir)
            val output = runner.build().output

            expectThat(output) {
                contains("> Task :createVersionPropertiesFile")
            }
        }

        @Test
        fun `creates a version_properties file containing all configured properties`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("clean", "createVersionPropertiesFile")
            runner.withProjectDir(projectDir)
            runner.build()

            expectThat(versionPropertiesFile()) {
                get(File::exists).isTrue()
                expectThat(subject.readText()) {
                    contains("applicationName=my-app")
                    contains("versionMajor=1")
                    contains("versionMinor=2")
                    contains("versionPatch=3")
                    contains("buildNumber=4")
                    contains("version=1.2.3")
                    contains("versionComplete=1.2.3-4")
                    contains(Regex("gitSha=.{7}\n"))
                    contains("buildDate=2024-01-10T01\\:02\\:03.456Z")
                    contains(Regex("gitBranch=.{1,}\n"))
                    contains("extraProperties.extraProp1=extraProp1.value")
                }
            }
        }

        @Test
        fun `is up-to-date when executed multiple times with an unmodified configuration`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("clean", "createVersionPropertiesFile")
            runner.withProjectDir(projectDir)
            runner.build()

            val runner2 = GradleRunner.create()
            runner2.forwardOutput()
            runner2.withPluginClasspath()
            runner2.withArguments("createVersionPropertiesFile")
            runner2.withProjectDir(projectDir)
            val result = runner2.build()
            expectThat(result.output) {
                contains("> Task :createVersionPropertiesFile UP-TO-DATE")
            }

        }
    }

    @Nested
    @DisplayName("deleteVersionPropertiesFile task")
    inner class DeleteVersionPropertiesFileTask {
        @Test
        fun `can be run`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("deleteVersionPropertiesFile")
            runner.withProjectDir(projectDir)
            val output = runner.build().output

            expectThat(output) {
                contains("> Task :deleteVersionPropertiesFile")
            }
        }

        @Test
        fun `deletes an existing version_properties file`() {
            val runner = GradleRunner.create()
            runner.forwardOutput()
            runner.withPluginClasspath()
            runner.withArguments("createVersionPropertiesFile", "deleteVersionPropertiesFile")
            runner.withProjectDir(projectDir)
            runner.build()
            expectThat(versionPropertiesFile().exists()) { isFalse() }
        }

    }
}
