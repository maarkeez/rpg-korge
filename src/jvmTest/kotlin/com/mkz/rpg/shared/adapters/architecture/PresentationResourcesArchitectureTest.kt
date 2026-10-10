package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
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
        assertThat(presentationFiles).isNotEmpty
        SoftAssertions.assertSoftly { softly ->
            filesReadingResources.forEach { file ->
                softly.fail<Unit>(
                    "File '%s' reads resources directly. Use '%s' to access the loaded resources instead.",
                    file.path,
                    resourcesLoaderFileName.removeSuffix(".kt"),
                )
            }
        }
    }
}
