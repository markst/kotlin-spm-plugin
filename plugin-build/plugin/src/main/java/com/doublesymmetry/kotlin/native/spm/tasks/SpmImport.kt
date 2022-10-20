package com.doublesymmetry.kotlin.native.spm.tasks

import com.doublesymmetry.kotlin.native.spm.plugin.KotlinSpmPlugin
import org.gradle.api.DefaultTask

abstract class SpmImport : DefaultTask() {
    init {
        description = "Add all Swift Package dependencies"
        group = KotlinSpmPlugin.TASK_GROUP
    }
}