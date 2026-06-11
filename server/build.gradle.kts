plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "me.nekoalice.mafia.api"
version = "0.1.0"

dependencies {
    implementation(project(":apiContract"))
    implementation(project(":dao"))
    implementation(project(":dto"))

    implementation(libs.bundles.server.ktor.server)
    implementation(libs.bundles.server.ktor.server.plugins)
    implementation(libs.bundles.server.projectDependencies)
    implementation(libs.bundles.server.peerDependencies)

    testImplementation(libs.bundles.server.ktor.test.plugins)
    testImplementation(libs.bundles.server.projectDependencies.test)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}
