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
                api(projects.icePortalClient)
                api(libs.kotlin.logging)
                api(libs.kotlinx.coroutines)
            }
        }

        getByName("jvmMain") {
            dependencies {
                implementation(libs.slf4j.simple)
                api(libs.jpx)
            }
        }
    }
}
