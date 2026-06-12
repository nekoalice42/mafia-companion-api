plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(project(":dto"))
    implementation(libs.bundles.apiContract.projectDependencies)
    implementation(libs.bundles.apiContract.peerDependencies)

    testImplementation(libs.bundles.apiContract.projectDependencies.test)
}
