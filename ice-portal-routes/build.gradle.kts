plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
