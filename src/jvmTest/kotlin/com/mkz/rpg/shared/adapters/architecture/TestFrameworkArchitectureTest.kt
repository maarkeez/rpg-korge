package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class TestFrameworkArchitectureTest {
    // Tests are declared with kotlin.test only (e.g.: `@Test`, `@BeforeTest`), which runs on every target, so the tests can be shared
    // through `commonTest`. JUnit features such as `@Nested` classes are not available on every target.
    private val forbiddenPackage = "org" + ".junit."

    @Test
    fun `should not use JUnit when is a test file`() {
        // Given
        val files = Konsist.scopeFromTest().files
        // When
        val filesUsingJUnit = files.filter { file -> file.text.contains(forbiddenPackage) }
        // Then
        files.shouldNotBeEmpty()
        val violations = filesUsingJUnit.map { file -> "Test file '${file.path}' uses JUnit. Use kotlin.test instead (e.g.: `kotlin.test.Test`)." }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
