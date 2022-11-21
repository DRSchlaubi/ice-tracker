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
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
