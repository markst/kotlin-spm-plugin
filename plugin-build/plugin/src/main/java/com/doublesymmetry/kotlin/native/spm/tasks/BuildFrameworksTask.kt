package com.doublesymmetry.kotlin.native.spm.tasks

import com.doublesymmetry.kotlin.native.spm.plugin.KotlinSpmPlugin
import com.doublesymmetry.kotlin.native.spm.swiftPackageBuildDirs
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.Family
import java.io.File

@CacheableTask
abstract class BuildFrameworksTask : DefaultTask() {
    init {
        /**
         * Task like a command: `xcodebuild -scheme IOS -sdk iphoneos -destination "platform=iOS Simulator" -configuration Release -derivedDataPath ./build SKIP_INSTALL=NO BUILD_LIBRARY_FOR_DISTRIBUTION=YES OTHER_SWIFT_FLAGS=-no-verify-emitted-module-interface SYMROOT=$(PWD)/build
         */
        description =
            "Build and assemble an xcframework from the Swift package, including headers and modules."
        group = KotlinSpmPlugin.TASK_GROUP
    }

    @Input
    val platformFamily: Property<Family> = project.objects.property(Family::class.java)

    @Input
    val platformDependency: Property<String> = project.objects.property(String::class.java)

    @get:OutputDirectory
    val outputFrameworkDirectory: Provider<File>
        get() = platformFamily.map {
            project.swiftPackageBuildDirs.releaseDir(it)
                .resolve("${platformDependency.get()}.framework")
        }

    @TaskAction
    fun buildXCFramework() {
        val configuration = "Release"
        val sdk = platformFamily.get().toSdk()

        buildFramework(configuration, sdk)
        copyHeaders(configuration, sdk)
        handleSwiftModules(configuration, sdk)
        copyResources(configuration, sdk)
        createXCFramework(configuration, sdk)
    }

    private fun buildFramework(configuration: String, sdk: String) {
        val packageDirectory = project.swiftPackageBuildDirs.platformRoot(platformFamily.get())
        val family = platformFamily.get()
        val familyPlatform = platformFamily.get().toPlatform()

        project.exec {
            println("packageDirectory: $packageDirectory")
            println("outputFrameworkDirectory: ${outputFrameworkDirectory.get()}")
            it.workingDir = packageDirectory
            it.commandLine(
                "xcodebuild", "build",
                "-scheme", family,
                "-destination", "generic/platform=$familyPlatform",
                "-sdk", sdk,
                "-configuration", configuration,
                "-derivedDataPath", project.swiftPackageBuildDirs.derivedDataPath(),
                "-quiet",
                "SKIP_INSTALL=NO",
                "BUILD_LIBRARY_FOR_DISTRIBUTION=YES",
                "OTHER_SWIFT_FLAGS=-no-verify-emitted-module-interface",
                "SYMROOT=${outputFrameworkDirectory.get().path}"
            )
        }
    }

    private fun copyHeaders(configuration: String, sdk: String) {
        val derivedDataPath = project.swiftPackageBuildDirs.derivedDataPath()
        val packageDirectory = project.swiftPackageBuildDirs.platformRoot(platformFamily.get())
        val family = platformFamily.get()
        val headersPath =
            outputFrameworkDirectory.get().resolve("${configuration}-${sdk}/PackageFrameworks/${family.name}.framework/Headers")
        headersPath.mkdirs()

        val buildFrameworkPath = File(
            derivedDataPath,
            "Build/Intermediates.noindex/${family}.build/${configuration}-${sdk}/${family}.build/Objects-normal/arm64"
        )
        val swiftHeader = File(buildFrameworkPath, "${family}-Swift.h")

        if (swiftHeader.exists()) {
            swiftHeader.copyTo(File(headersPath, "${family}-Swift.h"), overwrite = true)
        }

        val packageHeaders =
            packageDirectory.resolve("Sources/${family}/include")
        if (packageHeaders.exists()) {
            packageHeaders.copyRecursively(headersPath, overwrite = true)
        }
    }

    private fun handleSwiftModules(configuration: String, sdk: String) {
        val derivedDataPath = project.swiftPackageBuildDirs.derivedDataPath()
        val packageDirectory = project.swiftPackageBuildDirs.platformRoot(platformFamily.get())
        val family = platformFamily.get()
        val modulesDir =
            outputFrameworkDirectory.get().resolve("${configuration}-${sdk}/PackageFrameworks/${family.name}.framework/Modules")
        modulesDir.mkdirs()

        val swiftModuleDir =
            File(derivedDataPath, "${family}.swiftmodule")
        if (swiftModuleDir.exists()) {
            swiftModuleDir.copyRecursively(modulesDir, overwrite = true)
        } else {
            File(modulesDir, "module.modulemap").writeText(
                """
                framework module ${family} {
                    umbrella "Headers"
                    export *
                    module * { export * }
                }
            """.trimIndent()
            )
        }
    }

    private fun copyResources(configuration: String, sdk: String) {
        val derivedDataPath = project.swiftPackageBuildDirs.derivedDataPath()
        val packageDirectory = project.swiftPackageBuildDirs.platformRoot(platformFamily.get())
        val family = platformFamily.get()
        val bundleDir =
            File(derivedDataPath, "${family}_${family}.bundle")
        if (bundleDir.exists()) {
            bundleDir.copyRecursively(
                outputFrameworkDirectory.get().resolve("${platformDependency.get()}.framework"),
                overwrite = true
            )
        }
    }

    private fun createXCFramework(configuration: String, sdk: String) {
        val family = platformFamily.get()
        val frameworkDir = outputFrameworkDirectory.get().resolve("${configuration}-${sdk}/PackageFrameworks/${family.name}.framework")
        val xcframeworkPath = project.swiftPackageBuildDirs
            .releaseDir(family)
            .resolve("${platformDependency.get()}.xcframework")

        project.exec {
            it.commandLine(
                "xcodebuild", "-create-xcframework",
                "-framework", frameworkDir, // TODO: Pass list of dirs for each platformFamily / releaseDir / toReleaseSdk?
                "-output", xcframeworkPath.absolutePath
            )
        }
    }

    private fun Family.toSdk(): String {
        return when (this) {
            Family.OSX -> "macosx"
            Family.IOS -> "iphonesimulator"
            else -> ""
        }
    }

    private fun Family.toPlatform(): String {
        return when (this) {
            Family.OSX -> "Mac"
            Family.IOS -> "iOS Simulator"
            else -> "Unknown"
        }
    }
}