package com.doublesymmetry.kotlin.native.spm.tasks

import com.doublesymmetry.kotlin.native.spm.plugin.KotlinSpmPlugin
import com.doublesymmetry.kotlin.native.spm.swiftPackageBuildDirs
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.Family
import java.io.File

@CacheableTask
abstract class GenerateXcodeTask : Exec() {
    init {
        /**
         * Task like a command: `swift package generate-xcodeproj`
         */
        description = "Generate Xcode project"
        group = KotlinSpmPlugin.TASK_GROUP
    }

    @Input
    val platformFamily: Property<Family> = project.objects.property(Family::class.java)

    @get:OutputFile
    val outputPackageResolvedFile: Provider<File>
        get() = platformFamily.map {
            project.swiftPackageBuildDirs.packageResolvedFile(it)
        }

    @get:OutputDirectory
    val outputXcodeProjectFile: Provider<File>
        get() = platformFamily.map {
            project.swiftPackageBuildDirs.xcodeProjectFile(it)
        }

    override fun exec() {
        workingDir = project.swiftPackageBuildDirs.platformRoot(platformFamily.get())
        commandLine("swift", "package", "generate-xcodeproj")

        super.exec()
    }
}
