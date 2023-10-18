pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 添加jitpack仓库：拉取auikit库
        maven { url = java.net.URI.create("https://www.jitpack.io") }
    }
}

rootProject.name = "VoiceRoomApp"
include(":app")
// 添加asceneskit库到项目里
include(":asceneskit")
