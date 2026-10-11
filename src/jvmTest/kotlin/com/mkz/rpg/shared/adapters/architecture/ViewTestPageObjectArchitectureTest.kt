package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class ViewTestPageObjectArchitectureTest {
    // Tests running views (view tests and end-to-end tests) must only access view internals through the
    // view page objects (`<View>Extension.kt` files), which are the only test files allowed to use them.
    private val forbiddenImportPrefixes = listOf("korlibs.korge.view.", "korlibs.korge.ui.", "korlibs.korge.input.")
    private val forbiddenCalls = listOf("findViewByName(", ".children", "simulateClick(", "mouseMoveAndClickTo(")

    private val viewTestFiles: List<KoFileDeclaration> =
        Konsist
            .scopeFromTest()
            .files
            .filterNot { file -> file.name.endsWith("Extension") }
            .filter { file -> file.hasImport { it.name == "korlibs.korge.tests.ViewsForTesting" } }

    @Test
    fun `should not import view classes when is a test using the views test harness`() {
        // Given
        val files = viewTestFiles
        // When
        val forbiddenImportsByFile =
            files
                .associateWith { file -> file.imports.map { it.name }.filter { name -> forbiddenImportPrefixes.any { name.startsWith(it) } } }
                .filterValues { it.isNotEmpty() }
        // Then
        assertThat(files).isNotEmpty
        SoftAssertions.assertSoftly { softly ->
            forbiddenImportsByFile.forEach { (file, forbiddenImports) ->
                softly.fail<Unit>(
                    "Test '%s' imports view classes %s. Interact with views through their page object ('<View>Extension.kt') instead.",
                    file.name,
                    forbiddenImports,
                )
            }
        }
    }

    @Test
    fun `should not access view internals when is a test using the views test harness`() {
        // Given
        val files = viewTestFiles
        // When
        val forbiddenCallsByFile =
            files
                .associateWith { file -> forbiddenCalls.filter { call -> file.text.contains(call) } }
                .filterValues { it.isNotEmpty() }
        // Then
        assertThat(files).isNotEmpty
        SoftAssertions.assertSoftly { softly ->
            forbiddenCallsByFile.forEach { (file, calls) ->
                softly.fail<Unit>(
                    "Test '%s' accesses view internals using %s. Interact with views through their page object ('<View>Extension.kt') instead.",
                    file.name,
                    calls,
                )
            }
        }
    }
}
