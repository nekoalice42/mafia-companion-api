dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

private val projects = arrayOf(
    ":apiContract",
    ":cleaner",
    ":dto",
    ":dao",
    ":migrations",
    ":server",
)

include(*projects)
// https://docs.gradle.org/current/userguide/multi_project_builds.html#include_existing_projects_only
projects.forEach { project(it).projectDir.mkdirs() }

rootProject.name = "mafia-companion-api"
