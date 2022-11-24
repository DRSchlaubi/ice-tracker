plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(projects.icePortalModels)
    implementation(projects.icePortalRoutes)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.content.negotioation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.slf4j.simple)
}
