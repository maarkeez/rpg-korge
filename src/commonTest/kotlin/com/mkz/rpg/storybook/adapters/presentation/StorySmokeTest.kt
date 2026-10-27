package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother
import com.mkz.rpg.screen.adapters.presentation.TextsMother
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

/** Renders every story of the Storybook, as it is shown, so a story that fails is found before it is opened. */
class StorySmokeTest : ViewsForTesting() {
    @Test
    fun `Render should render every story of every story binding when they are shown with the game resources and texts`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            // When
            val failures =
                StorybookApi.STORY_BINDINGS.flatMap { binding ->
                    binding.component.stories.mapNotNull { story ->
                        runCatching { addChild(binding.render(story.id, storyContext)) }.exceptionOrNull()?.let { failure -> "${story.id}: $failure" }
                    }
                }
            // Then
            withClue("Stories that fail to render") { failures.shouldBeEmpty() }
        }

    @Test
    fun `StoryBindings should keep the ids of every component and story, as the links shared hold them`() {
        // When
        val ids = StorybookApi.STORY_BINDINGS.flatMap { binding -> listOf(binding.component.id) + binding.component.stories.map { story -> story.id } }
        // Then
        withClue("Renaming an id breaks the links shared: keep it, even when its component or story is renamed") {
            ids.shouldContainExactly(
                "screen-health-bar",
                "screen-health-bar--full",
                "screen-health-bar--half",
                "screen-health-bar--empty",
            )
        }
    }
}
