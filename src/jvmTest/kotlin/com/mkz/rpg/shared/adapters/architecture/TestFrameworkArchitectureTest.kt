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

    // KorGE's `viewsTest` skips the test on JS by default, so a view test in `commonTest` would pass on JS without asserting anything.
    // `sharedViewsTest` runs it on every target instead.
    private val viewsTestCall = Regex("""\bviewsTest\s*[({]""")
    private val sharedViewsTestFileName = "SharedViewsTestExtension"

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
        val sharedFilesCallingViewsTest =
            sharedFiles.filterNot { file -> file.name == sharedViewsTestFileName }.filter { file -> viewsTestCall.containsMatchIn(file.text) }
        // Then
        sharedFiles.shouldNotBeEmpty()
        val violations =
            sharedFilesCallingViewsTest.map { file -> "Shared test file '${file.path}' uses KorGE's `viewsTest`, which is skipped on JS. Use `sharedViewsTest` instead." }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
