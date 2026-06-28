plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":dao"))
    implementation(libs.bundles.cleaner.projectDependencies)
    implementation(libs.bundles.cleaner.peerDependencies)
}

application {
    mainClass = "me.nekoalice.mafia.api.cleaner.MainKt"
}
