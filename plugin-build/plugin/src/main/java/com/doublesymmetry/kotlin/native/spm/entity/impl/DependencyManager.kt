package com.doublesymmetry.kotlin.native.spm.entity.impl

import com.doublesymmetry.kotlin.native.spm.entity.DependencyMarker
import org.gradle.api.Named
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

@DependencyMarker
class DependencyManager {
    val dependencies = mutableListOf<Package>()

    fun packages(url: String, version: String, name: String) {
        val dependency = Package(url, version, name)
        dependencies.add(dependency)
    }

    fun packages(url: String, version: String) {
        val dependency = Package(url, version)
        dependencies.add(dependency)
    }

    fun packages(path: String) {
        val dependency = Package(path)
        dependencies.add(dependency)
    }

    data class Package(
        @Input val url: String,
        @Input @Optional val version: String? = null,
        @Input val dependencyName: String = url
            .subSequence(url.lastIndexOf("/") + 1, url.length - ".git".length)
            .toString(),
    ) : Named {
        @Internal
        override fun getName(): String = dependencyName

        fun convertToPackageContent(): String {
            return """
                .package(
                    name: "$dependencyName",
                    url: "$url",
                    from: "$version"
                )
            """.trimIndent()
        }
    }
}
