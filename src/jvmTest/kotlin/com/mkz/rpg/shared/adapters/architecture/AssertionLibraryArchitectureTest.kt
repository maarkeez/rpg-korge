package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class AssertionLibraryArchitectureTest {
    // Tests are asserted with Kotest assertions only, which run on every target, so the tests can be shared through `commonTest`.
    private val forbiddenPackage = "org" + ".assertj."

    @Test
    fun `should not use AssertJ when is a test file`() {
        // Given
        val files = Konsist.scopeFromTest().files
        // When
        val filesUsingAssertJ = files.filter { file -> file.text.contains(forbiddenPackage) }
        // Then
        files.shouldNotBeEmpty()
        val violations = filesUsingAssertJ.map { file -> "Test file '${file.path}' uses AssertJ. Use Kotest assertions instead." }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
