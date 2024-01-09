package de.macnix.gradle.gitversion

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

data class GitVersion(
    val version: String,
    val numberOfCommits: String,
    val sha: String,
    val branch: String
)

internal fun execShellCommand(vararg command: String): Optional<List<String>> {
    val process = Runtime.getRuntime().exec(command)
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val lines = reader.readLines()

    val exitVal = process.waitFor()
    return if (exitVal == 0) {
        Optional.of(lines)
    } else {
        Optional.empty()
    }
}

internal fun gitDescribe(): Triple<String, String, String> =
    execShellCommand("git", "describe", "--tags", "--long", "--always")
        .map {
            val gitDescribe = it.first().trim()
            Optional.ofNullable(
                Regex("(?<hasTag>(?<tag>.*)-(?<numCommits>[^-]*)-g)?(?<sha>[^-]*)").matchEntire(
                    gitDescribe
                )
            )
                .map { matchResult ->
                    val matchGroups = matchResult.groups
                    val version = matchGroups["tag"]?.value ?: "0.0"
                    val numCommits = matchGroups["numCommits"]?.value ?: "0"
                    val sha = matchGroups["sha"]?.value ?: ""
                    Triple(version, numCommits, sha)
                }
                .orElse(Triple("0.0", "0", ""))
        }.orElse(Triple("0.0", "0", ""))

internal fun gitBranch(): String =
    execShellCommand("git", "rev-parse", "--abbrev-ref", "HEAD").map {
        it.first().trim()
    }.orElse("<unknown branch>")

fun gitVersion(): GitVersion {
    val gitDescribe = gitDescribe()
    val gitBranch = gitBranch()
    val gitVersion = GitVersion(gitDescribe.first, gitDescribe.second, gitDescribe.third, gitBranch)
    return gitVersion
}

