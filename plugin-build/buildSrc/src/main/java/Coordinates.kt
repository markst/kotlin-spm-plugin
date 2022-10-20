object PluginCoordinates {
    const val GROUP = "com.doublesymmetry"
    const val ARTIFACT = "kotlin-native-spm"
    const val VERSION = "0.1.2"

    const val ID = "$GROUP.$ARTIFACT"
    const val IMPLEMENTATION_CLASS = "$GROUP.kotlin.native.spm.plugin.KotlinSpmPlugin"
}

object PluginBundle {
    const val VCS = "https://github.com/doublesymmetry/kotlin-spm-plugin"
    const val WEBSITE = "https://github.com/doublesymmetry/kotlin-spm-plugin"
    const val DESCRIPTION = "Gradle plugin for Swift Package Manager integration with Kotlin Multiplatform projects"
    const val DISPLAY_NAME = "Gradle Plugin for SPM integration with Kotlin MPP"

    val TAGS = listOf(
        "plugin",
        "gradle",
        "kotlin",
        "multiplatform",
        "spm"
    )
}

