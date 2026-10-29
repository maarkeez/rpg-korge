package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

/**
 * Every view of the game has its stories in the Storybook: a `<View>Stories` binding in the presentation package of the
 * storybook subdomain, listed in `StorybookApi`, with no exception. The views of the Storybook itself are left out.
 * See docs/architecture/package-structure.md.
 */
class StoryCoverageArchitectureTest {
    private val storybookPresentationPackage = GameViews.STORYBOOK_PRESENTATION_PACKAGE

    private val bindings: List<String> =
        Konsist
            .scopeFromProduction()
            .objects()
            .filter { declaration -> declaration.packagee?.fullyQualifiedName == storybookPresentationPackage && declaration.name.endsWith("Stories") }
            .map { declaration -> declaration.name }

    private val views: List<String> = GameViews.names

    @Test
    fun `should have its stories when it is a view of the game`() {
        // When
        val viewsMissingStories = views.filterNot { view -> "${view}Stories" in bindings }
        // Then
        views.shouldNotBeEmpty()
        withClue("Views without stories: $viewsMissingStories. Add a `<View>Stories` binding in $storybookPresentationPackage, listed in StorybookApi.") {
            viewsMissingStories.shouldBeEmpty()
        }
    }

    @Test
    fun `should bind a view and be listed in StorybookApi when it is a story binding`() {
        // Given
        val storybookApi = Konsist.scopeFromProduction().files.single { file -> file.packagee?.fullyQualifiedName == storybookPresentationPackage && file.name == "StorybookApi" }
        // When
        val bindingsOfNoView = bindings.filterNot { binding -> binding.removeSuffix("Stories") in views }
        val unlistedBindings = bindings.filterNot { binding -> Regex("""\b$binding\b""").containsMatchIn(storybookApi.text) }
        // Then
        bindings.shouldNotBeEmpty()
        withClue("Story bindings named after no view of the game: $bindingsOfNoView. Name them `<View>Stories`.") {
            bindingsOfNoView.shouldBeEmpty()
        }
        withClue("Story bindings not listed in StorybookApi.STORY_BINDINGS: $unlistedBindings. The Storybook and its smoke test would miss them.") {
            unlistedBindings.shouldBeEmpty()
        }
    }
}
