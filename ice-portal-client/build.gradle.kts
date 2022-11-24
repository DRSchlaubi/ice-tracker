plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    explicitApi()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.icePortalModels)
                api(projects.icePortalRoutes)
                api(libs.ktor.client.okhttp)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.resources)
                api(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}
