plugins {
    alias(libs.plugins.kotlin.mpp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    explicitApi()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.resources)
            }
        }
    }
}
