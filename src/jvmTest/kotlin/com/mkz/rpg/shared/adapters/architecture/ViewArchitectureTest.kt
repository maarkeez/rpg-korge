package com.mkz.rpg.shared.adapters.architecture

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

/**
 * The views of the game draw view data and never import the domain nor the use cases of a subdomain: their presenters
 * map the domain to the view data. So every view can have its stories in the Storybook, which feeds them plain data,
 * with no exception. See docs/architecture/package-structure.md.
 */
class ViewArchitectureTest {
    private val domainOrUseCasesImport = Regex("""^com\.mkz\.rpg\.[^.]+\.(domain|usecases)\..+""")

    @Test
    fun `should not import the domain nor the use cases when it is a view of the game`() {
        // Given
        val views = GameViews.declarations
        // When
        val forbiddenImports =
            views.flatMap { view ->
                view.containingFile.imports
                    .map { import -> import.name }
                    .filter { import -> domainOrUseCasesImport.matches(import) }
                    .map { import -> "$import (${view.name})" }
            }
        // Then
        views.shouldNotBeEmpty()
        withClue("Views importing the domain or the use cases: $forbiddenImports. Take view data instead, mapped from the domain by the presenter.") {
            forbiddenImports.shouldBeEmpty()
        }
    }
}
