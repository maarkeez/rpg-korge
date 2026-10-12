package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class MockingLibraryArchitectureTest {
    // Tests use real collaborators, in-memory repositories and hand-written fakes instead of mocks, so they can be shared through `commonTest`.
    private val forbiddenPackages = listOf("org" + ".mockito.", "io" + ".mockk.", "dev" + ".mokkery.")

    @Test
    fun `should not use a mocking library when is a test file`() {
        // Given
        val files = Konsist.scopeFromTest().files
        // When
        val filesUsingMocks = files.filter { file -> forbiddenPackages.any { forbiddenPackage -> file.text.contains(forbiddenPackage) } }
        // Then
        files.shouldNotBeEmpty()
        val violations = filesUsingMocks.map { file -> "Test file '${file.path}' uses a mocking library. Use real collaborators or fakes instead." }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }
}
