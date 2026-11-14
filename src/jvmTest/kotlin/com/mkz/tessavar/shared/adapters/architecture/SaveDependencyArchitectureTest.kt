package com.mkz.tessavar.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class SaveDependencyArchitectureTest {
    private val rootPackage = "com.mkz.tessavar"
    private val savePackage = "$rootPackage.save"
    private val screenPackage = "$rootPackage.screen"

    @Test
    fun `should only be imported by the screen subdomain when it is the save subdomain`() {
        // Given
        val files = Konsist.scopeFromProduction().files
        val outsideFiles = files.filterNot { it.packagee?.fullyQualifiedName?.let { name -> name == savePackage || name.startsWith("$savePackage.") } == true }
        // When
        val importingFiles = outsideFiles.filter { file -> file.imports.any { it.name.startsWith("$savePackage.") } }
        val forbiddenFiles = importingFiles.filterNot { file -> file.packagee?.fullyQualifiedName?.startsWith("$screenPackage.") == true }
        // Then
        importingFiles.shouldNotBeEmpty()
        withClue("Files out of the screen importing the save: ${forbiddenFiles.map { it.path }}. Hand the screen a Snapshottable instead.") {
            forbiddenFiles.shouldBeEmpty()
        }
    }
}
