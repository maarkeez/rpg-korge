package com.mkz.rpg.storybook.domain

import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class StorybookTest {
    private val full = story(id = "screen-health-bar--full", name = "Full")
    private val empty = story(id = "screen-health-bar--empty", name = "Empty")
    private val healthBar = component(id = "screen-health-bar", stories = listOf(full, empty))
    private val manaBar = component(id = "screen-mana-bar", title = "Mana bar", stories = listOf(story(id = "screen-mana-bar--full")))

    @Test
    fun `Create should list the components and open the first story when no story is requested`() {
        // When
        val storybook = Storybook.create(listOf(healthBar, manaBar), requestedStoryId = null)
        // Then
        storybook.toDto() shouldBe Storybook.Dto(components = listOf(healthBar, manaBar), selectedStoryId = "screen-health-bar--full")
    }

    @Test
    fun `Create should open the requested story when it is in the catalogue`() {
        // When
        val storybook = Storybook.create(listOf(healthBar, manaBar), requestedStoryId = "screen-health-bar--empty")
        // Then
        storybook.toDto().selectedStoryId shouldBe "screen-health-bar--empty"
    }

    @Test
    fun `Create should open the first story when the requested story is not in the catalogue`() {
        // When
        val storybook = Storybook.create(listOf(healthBar, manaBar), requestedStoryId = "screen-health-bar--renamed")
        // Then
        storybook.toDto().selectedStoryId shouldBe "screen-health-bar--full"
    }

    @Test
    fun `Create should publish the catalogue initialised, then the story selected, when the catalogue is created`() {
        // When
        val (events, _) = Storybook.create(listOf(healthBar), requestedStoryId = "screen-health-bar--empty").pullEvents()
        // Then
        events.shouldContainExactly(CatalogueInitialised, StorySelected(storyId = "screen-health-bar--empty"))
    }

    @Test
    fun `Create should fail when there is no component`() {
        // When
        val result = runCatching { Storybook.create(emptyList(), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.EmptyCatalogue>()
    }

    @Test
    fun `Create should fail when a component has no story`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(stories = emptyList())), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.ComponentWithoutStories>()
    }

    @Test
    fun `Create should fail when a component id is not in kebab case`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(id = "Screen-HealthBar")), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidComponentId>()
    }

    @Test
    fun `Create should fail when a story id does not start with its component id`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-mana-bar--full")))), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a story name is not in kebab case`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-health-bar--Full")))), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a story id has no name`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-health-bar--")))), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a component id is repeated`() {
        // When
        val result = runCatching { Storybook.create(listOf(healthBar, healthBar.copy(title = "Other")), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.RepeatedComponentId>()
    }

    @Test
    fun `Create should fail when a story id is repeated`() {
        // When
        val result = runCatching { Storybook.create(listOf(component(stories = listOf(full, full.copy(name = "Other")))), requestedStoryId = null) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.RepeatedStoryId>()
    }

    @Test
    fun `SelectStory should show the story and publish the story selected when it is another story of the catalogue`() {
        // Given
        val storybook = storybook(components = listOf(healthBar, manaBar))
        // When
        val (events, selectedStorybook) = storybook.selectStory("screen-mana-bar--full").pullEvents()
        // Then
        selectedStorybook.toDto().selectedStoryId shouldBe "screen-mana-bar--full"
        events.shouldContainExactly(StorySelected(storyId = "screen-mana-bar--full"))
    }

    @Test
    fun `SelectStory should change nothing when the story is already on show`() {
        // Given
        val storybook = storybook(components = listOf(healthBar), requestedStoryId = "screen-health-bar--empty")
        // When
        val (events, selectedStorybook) = storybook.selectStory("screen-health-bar--empty").pullEvents()
        // Then
        selectedStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `SelectStory should fail when the story is not in the catalogue`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val result = runCatching { storybook.selectStory("screen-mana-bar--full") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.StoryNotFound>()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // When
        val (events, _) = storybook().pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
