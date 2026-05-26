import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()

    jvm()
    js {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.dto.projectDependencies)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.dto.projectDependencies.test)
            }
        }
    }
}
