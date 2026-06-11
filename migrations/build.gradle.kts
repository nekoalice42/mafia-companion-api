plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(libs.bundles.migrations.projectDependencies)
    implementation(libs.bundles.migrations.peerDependencies)
}

application {
    mainClass = "me.nekoalice.mafia.migrations.MainKt"
}
