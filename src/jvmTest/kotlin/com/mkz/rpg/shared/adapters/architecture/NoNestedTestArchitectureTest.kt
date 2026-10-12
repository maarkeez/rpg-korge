package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class NoNestedTestArchitectureTest {
    // Nested test classes are not available on every target, so the tests name what they test instead
    // (e.g.: `StartFirstRound should ... when ...`), and can be shared through `commonTest`.
    private val forbiddenImport = "org.junit.jupiter.api." + "Nested"

    @Test
    fun `should not use nested test classes when is a test file`() {
        // Given
        val files = Konsist.scopeFromTest().files
        // When
        val filesUsingNested = files.filter { file -> file.hasImport { it.name == forbiddenImport } || file.text.contains("@$forbiddenImport") }
        // Then
        files.shouldNotBeEmpty()
        val violations = filesUsingNested.map { file -> "Test file '${file.path}' uses @Nested. Prefix the test names with what they test instead." }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
