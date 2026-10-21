package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class ResourcesBundleArchitectureTest {
    private val resourcesBundleFileName = "ResourcesBundle.kt"
    private val resourcesVfsImport = "korlibs.io.file.std.resourcesVfs"

    @Test
    fun `should not read the resource files one by one when is a production file other than the resources bundle`() {
        // Given
        val productionFiles = Konsist.scopeFromProduction().files
        // When
        val filesReadingResourceFiles =
            productionFiles
                .filterNot { it.nameWithExtension == resourcesBundleFileName }
                .filter { file -> file.imports.any { it.name == resourcesVfsImport } }
        // Then
        productionFiles.shouldNotBeEmpty()
        val violations =
            filesReadingResourceFiles.map { file ->
                "File '${file.path}' reads the resource files one by one. Read them from " +
                    "'${resourcesBundleFileName.removeSuffix(".kt")}.root' instead, so they are all loaded with a single request."
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
