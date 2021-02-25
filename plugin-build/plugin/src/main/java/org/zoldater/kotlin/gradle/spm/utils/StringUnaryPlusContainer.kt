/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.zoldater.kotlin.gradle.spm.utils

abstract class StringUnaryPlusContainer {
    val container = mutableListOf<String>()

    operator fun String.unaryPlus() {
        container.add(this)
    }
}
