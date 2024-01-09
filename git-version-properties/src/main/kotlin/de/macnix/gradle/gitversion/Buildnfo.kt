package de.macnix.gradle.gitversion

import java.time.ZonedDateTime

internal fun buildNumber() = System.getenv("BUILD_NUMBER")?.toString() ?: "0"

internal fun buildDate() = ZonedDateTime.now()

fun buildInfo() = Pair(buildNumber(), buildDate())
