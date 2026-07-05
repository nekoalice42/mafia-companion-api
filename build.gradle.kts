import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

subprojects {
    val args = listOf(
        "-Xallow-returns-result-of",
        "-Xreturn-value-checker=full",
        "-Xcontext-sensitive-resolution",
    )
    tasks.withType<KotlinCompile>().all {
        compilerOptions.freeCompilerArgs.addAll(args)
    }
    tasks.withType<KotlinCompileCommon>().all {
        compilerOptions.freeCompilerArgs.addAll(args)
    }
}
