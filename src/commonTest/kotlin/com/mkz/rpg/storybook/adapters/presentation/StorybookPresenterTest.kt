package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.screen.adapters.presentation.TextsMother
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class StorybookPresenterTest {
    private val eventBus = InMemoryEventBus()
    private val sidebarView = FakeSidebarView()
    private val storyInfoView = FakeStoryInfoView()
    private val phoneFrameView = FakePhoneFrameView()

    // The health bar needs no image, so the resources are not loaded.
    private val storyContext = StoryContext(BattleSceneResourcesLoader(), TextsMother.texts())

    private fun startStorybook(storyLocation: FakeStoryLocation = FakeStoryLocation()): StorybookApi {
        val storybookApi = StorybookApi(eventBus, storyLocation)
        StorybookPresenter(sidebarView, storyInfoView, phoneFrameView, storybookApi, storyContext, eventBus)
        storybookApi.init()
        eventBus.dispatch()
        return storybookApi
    }

    @Test
    fun `CatalogueInitialised should list the catalogue in the sidebar when the catalogue is initialised`() {
        // When
        val storybookApi = startStorybook()
        // Then
        sidebarView.displayedCatalogues shouldBe listOf(storybookApi.searchCatalogue())
    }

    @Test
    fun `StorySelected should show the story the location asks for, highlighted and described, when the catalogue is initialised`() {
        // When
        startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--half"))
        // Then
        sidebarView.selectedStoryIds.shouldContainExactly("screen-health-bar--half")
        storyInfoView.displayedStories.map { (component, story) -> component.title to story.name }.shouldContainExactly("Health bar" to "Half")
        phoneFrameView.displayedViews
            .single()
            .shouldBeInstanceOf<HealthBarView>()
            .labelText() shouldBe "15 / 30"
    }

    @Test
    fun `StorySelected should show the story, and keep it in the location, when it is tapped in the sidebar`() {
        // Given
        val storyLocation = FakeStoryLocation()
        startStorybook(storyLocation)
        // When
        sidebarView.delegate.shouldNotBeNull().storySelected("screen-health-bar--empty")
        eventBus.dispatch()
        // Then
        sidebarView.selectedStoryIds.shouldContainExactly("screen-health-bar--full", "screen-health-bar--empty")
        phoneFrameView.displayedViews
            .last()
            .shouldBeInstanceOf<HealthBarView>()
            .labelText() shouldBe "0 / 30"
        storyLocation.keptStoryIds.shouldContainExactly("screen-health-bar--empty")
    }
}
