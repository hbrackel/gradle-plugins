import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "de.macnix.gradle"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.strikt:strikt-core:0.34.0")
}

gradlePlugin {
    val gitVersion by plugins.creating {
        id = "de.macnix.gradle.git-version-properties"
        implementationClass = "de.macnix.gradle.gitversion.GitVersionPropertiesPlugin"
        displayName = "Git-Version-Properties Gradle Plugin"
        description = "A plugin providing automatic versioning of CI/CD builds based on git tags and build info"

        vcsUrl = "https://github.com/hbrackel/gradle-plugins"
        website = "https://github.com/hbrackel/gradle-plugins"

        tags = listOf("git", "versioning", "cicd", "plugins")
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}