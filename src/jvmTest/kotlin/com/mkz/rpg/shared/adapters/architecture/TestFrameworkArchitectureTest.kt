package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class TestFrameworkArchitectureTest {
    private val forbiddenPackage = "org" + ".junit."

    private val viewsTestCall = Regex("""\bviewsTest\s*[({]""")

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

    @Test
    fun `should use sharedViewsTest when is a shared view test`() {
        // Given
        val sharedFiles = Konsist.scopeFromTest().files.filter { file -> file.path.contains("/src/commonTest/") }
        // When
        val sharedFilesCallingViewsTest = sharedFiles.filter { file -> viewsTestCall.containsMatchIn(file.text) }
        // Then
        sharedFiles.shouldNotBeEmpty()
        val violations =
            sharedFilesCallingViewsTest.map { file ->
                "Shared test file '${file.path}' uses KorGE's `viewsTest`, which is skipped on JS. Use `sharedViewsTest` instead, which runs it on every target."
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
