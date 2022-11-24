pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ICETracker"
include(
    ":app",
    ":ice-portal-models",
    ":ice-portal-routes",
    ":ice-portal-client",
    ":ice-portal-fetcher",
    ":test-server"
)
