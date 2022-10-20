package com.doublesymmetry.kotlin.native.spm.utils

@Suppress("UnnecessaryAbstractClass")
abstract class StringUnaryPlusContainer {
    val container = mutableListOf<String>()

    operator fun String.unaryPlus() {
        container.add(this)
    }
}
