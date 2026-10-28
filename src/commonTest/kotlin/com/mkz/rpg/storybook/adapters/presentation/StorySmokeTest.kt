package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother
import com.mkz.rpg.screen.adapters.presentation.TextsMother
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

/** Renders every story of the Storybook, as it is shown, so a story that fails is found before it is opened. */
class StorySmokeTest : ViewsForTesting() {
    @Test
    fun `Render should render every story of every story binding when they are shown with their default args, the game resources and texts`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            // When
            val failures =
                StorybookApi.STORY_BINDINGS.flatMap { binding ->
                    val component = binding.component(storyContext)
                    component.stories.mapNotNull { story ->
                        runCatching { addChild(binding.render(Args(component.args + story.args), storyContext)) }.exceptionOrNull()?.let { failure -> "${story.id}: $failure" }
                    }
                }
            // Then
            withClue("Stories that fail to render") { failures.shouldBeEmpty() }
        }

    @Test
    fun `StoryBindings should keep the ids of every component and story, as the links shared hold them`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            // When
            val components = StorybookApi.STORY_BINDINGS.map { binding -> binding.component(storyContext) }
            val ids = components.flatMap { component -> listOf(component.id) + component.stories.map { story -> story.id } }
            // Then
            withClue("Renaming an id breaks the links shared: keep it, even when its component or story is renamed") {
                ids.shouldContainExactly(
                    "screen-health-bar",
                    "screen-health-bar--full",
                    "screen-health-bar--half",
                    "screen-health-bar--empty",
                    "screen-inventory-slot",
                    "screen-inventory-slot--empty",
                    "screen-inventory-slot--stored",
                    "screen-inventory-slot--equipped",
                    "screen-inventory-slot--selected",
                    "screen-inventory-slot--dimmed",
                )
            }
        }

    @Test
    fun `Init should open the catalogue of every story binding when their controls, args and stories are valid`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            val storybookApi = StorybookApi(InMemoryEventBus(), FakeStoryLocation())
            // When
            storybookApi.init(storyContext)
            // Then
            storybookApi.searchCatalogue().size shouldBe StorybookApi.STORY_BINDINGS.size
        }
}
