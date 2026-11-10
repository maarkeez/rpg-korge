package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class LocalizationArchitectureTest {
    private val rootPackage = "com.mkz.rpg"
    private val localizationPackage = "$rootPackage.localization"

    @Test
    fun `should only be imported by presentation packages when it is the localization subdomain`() {
        // Given
        val files = Konsist.scopeFromProduction().files
        val outsideFiles = files.filterNot { it.packagee?.fullyQualifiedName?.startsWith(localizationPackage) == true }
        // When
        val importingFiles = outsideFiles.filter { file -> file.imports.any { it.name.startsWith("$localizationPackage.") } }
        val forbiddenFiles =
            importingFiles.filterNot { file ->
                val packageName = file.packagee?.fullyQualifiedName
                packageName == rootPackage || packageName?.endsWith(".adapters.presentation") == true
            }
        // Then
        importingFiles.shouldNotBeEmpty()
        withClue("Files translating out of a presentation package or the root package: ${forbiddenFiles.map { it.path }}. Hold ids instead, and let the screen translate them.") {
            forbiddenFiles.shouldBeEmpty()
        }
    }
}
