package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class PresentationResourcesArchitectureTest {
    private val resourcesLoaderFileName = "BattleSceneResourcesLoader.kt"
    private val resourceReadingImports =
        setOf(
            "korlibs.io.file.std.resourcesVfs",
            "korlibs.image.format.readBitmap",
        )

    @Test
    fun `should not read resources when is a presentation file other than the resources loader`() {
        // Given
        val presentationFiles =
            Konsist
                .scopeFromProduction()
                .files
                .filter { it.packagee?.fullyQualifiedName?.contains(".adapters.presentation") == true }
        // When
        val filesReadingResources =
            presentationFiles
                .filterNot { it.nameWithExtension == resourcesLoaderFileName }
                .filter { file -> file.imports.any { it.name in resourceReadingImports } }
        // Then
        presentationFiles.shouldNotBeEmpty()
        val violations =
            filesReadingResources.map { file ->
                "File '${file.path}' reads resources directly. Use '${resourcesLoaderFileName.removeSuffix(".kt")}' to access the loaded resources instead."
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
