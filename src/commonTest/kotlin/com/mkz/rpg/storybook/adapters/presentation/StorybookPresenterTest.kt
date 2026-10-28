package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.screen.adapters.presentation.TextsMother
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.storybook.adapters.page.FakeStorybookPage
import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookMother.location
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class StorybookPresenterTest {
    private val eventBus = InMemoryEventBus()
    private val sidebarView = FakeSidebarView()
    private val toolbarView = FakeToolbarView()
    private val storyInfoView = FakeStoryInfoView()
    private val controlsView = FakeControlsView()
    private val actionsView = FakeActionsView()
    private val phoneFrameView = FakePhoneFrameView()
    private val storybookPage = FakeStorybookPage()

    // The health bar needs no image, so the resources are not loaded.
    private val storyContext = StoryContext(BattleSceneResourcesLoader(), TextsMother.texts())

    private fun startStorybook(storyLocation: FakeStoryLocation = FakeStoryLocation()): StorybookApi {
        val storybookApi = StorybookApi(eventBus, storyLocation, storyBindings = listOf(HealthBarViewStories))
        StorybookPresenter(sidebarView, toolbarView, storyInfoView, controlsView, actionsView, phoneFrameView, storybookApi, storyContext, storybookPage, eventBus)
        storybookApi.init(storyContext)
        eventBus.dispatch()
        return storybookApi
    }

    private fun displayedHealthBarLabel(): String =
        phoneFrameView.displayedViews
            .last()
            .shouldBeInstanceOf<HealthBarView>()
            .labelText()

    @Test
    fun `CatalogueInitialised should list the catalogue in the sidebar when the catalogue is initialised`() {
        // When
        val storybookApi = startStorybook()
        // Then
        sidebarView.displayedCatalogues shouldBe listOf(storybookApi.searchCatalogue())
    }

    @Test
    fun `StorySelected should show the story the location asks for, highlighted, described and with its controls, when the catalogue is initialised`() {
        // When
        startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--half"))
        // Then
        sidebarView.selectedStoryIds.shouldContainExactly("screen-health-bar--half")
        storyInfoView.displayedStories.map { (component, story) -> component.title to story.name }.shouldContainExactly("Health bar" to "Half")
        controlsView.displayedControls.map { control -> control.name }.shouldContainExactly("remaining", "maximum")
        controlsView.displayedArgs.shouldContainExactly(mapOf("remaining" to ArgDto.int(15), "maximum" to ArgDto.int(30)))
        toolbarView.displayedLanguages.map { language -> language.code } shouldBe storyContext.texts.languages().map { language -> language.code }
        toolbarView.selectedLanguageCodes.shouldContainExactly("en")
        phoneFrameView.displayedViews.size shouldBe 1
        displayedHealthBarLabel() shouldBe "15 / 30"
        actionsView.displayedActions shouldBe listOf(emptyList())
        storybookPage.renderedStoryIds.shouldContainExactly("screen-health-bar--half")
    }

    @Test
    fun `StorySelected should show the story with the args and language the location asks for when the catalogue is initialised`() {
        // When
        startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--half", requestedArgs = mapOf("maximum" to "40"), requestedGlobals = mapOf("language" to "es")))
        // Then
        displayedHealthBarLabel() shouldBe "15 / 40"
        toolbarView.selectedLanguageCodes.shouldContainExactly("es")
        storyContext.texts[UiText.SHOP_NOT_ENOUGH_GOLD] shouldBe "No hay suficiente oro"
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
        displayedHealthBarLabel() shouldBe "0 / 30"
        storyLocation.keptStoryIds.shouldContainExactly("screen-health-bar--empty")
        storybookPage.renderedStoryIds.shouldContainExactly("screen-health-bar--full", "screen-health-bar--empty")
    }

    @Test
    fun `ArgChanged should show the story again with the arg, and keep it in the location, when a control is edited`() {
        // Given
        val storyLocation = FakeStoryLocation()
        startStorybook(storyLocation)
        // When
        controlsView.delegate.shouldNotBeNull().argChanged("remaining", ArgDto.int(7))
        eventBus.dispatch()
        // Then
        displayedHealthBarLabel() shouldBe "7 / 30"
        controlsView.displayedArgs.last() shouldBe mapOf("remaining" to ArgDto.int(7), "maximum" to ArgDto.int(30))
        storybookPage.renderedStoryIds.shouldContainExactly("screen-health-bar--full", "screen-health-bar--full")
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full", args = mapOf("remaining" to "7")))
    }

    @Test
    fun `ArgsReset should show the story again with its own args when the controls are reset`() {
        // Given
        startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--full", requestedArgs = mapOf("remaining" to "7")))
        // When
        controlsView.delegate.shouldNotBeNull().argsReset()
        eventBus.dispatch()
        // Then
        displayedHealthBarLabel() shouldBe "30 / 30"
    }

    @Test
    fun `GlobalChanged should show the story again in the language, and keep it in the location, when a language is tapped in the toolbar`() {
        // Given
        val storyLocation = FakeStoryLocation()
        startStorybook(storyLocation)
        // When
        toolbarView.delegate.shouldNotBeNull().languageSelected("es")
        eventBus.dispatch()
        // Then
        toolbarView.selectedLanguageCodes.shouldContainExactly("en", "es")
        phoneFrameView.displayedViews.size shouldBe 2
        storyContext.texts[UiText.SHOP_NOT_ENOUGH_GOLD] shouldBe "No hay suficiente oro"
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full", globals = mapOf("language" to "es")))
    }

    @Test
    fun `ActionRecorded should list the action, without showing the story again, when the view on show makes a callback`() {
        // Given
        startStorybook()
        // When
        storyContext.action("barTapped", 3)
        eventBus.dispatch()
        // Then
        actionsView.displayedActions.last().shouldContainExactly(ActionDto(name = "barTapped", arguments = listOf("3")))
        phoneFrameView.displayedViews.size shouldBe 1
        storybookPage.renderedStoryIds.shouldContainExactly("screen-health-bar--full")
    }

    @Test
    fun `ActionRecorded should list the action and show the story again with the arg it is mapped to, keeping it in the location`() {
        // Given
        val storyLocation = FakeStoryLocation()
        startStorybook(storyLocation)
        // When
        storyContext.action("barTapped", 7, setArg = "remaining" to ArgDto.int(7))
        eventBus.dispatch()
        // Then
        actionsView.displayedActions.last().shouldContainExactly(ActionDto(name = "barTapped", arguments = listOf("7")))
        displayedHealthBarLabel() shouldBe "7 / 30"
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full", args = mapOf("remaining" to "7")))
    }

    @Test
    fun `ActionsCleared should list no action when the actions are cleared`() {
        // Given
        startStorybook()
        storyContext.action("barTapped", 3)
        eventBus.dispatch()
        // When
        actionsView.delegate.shouldNotBeNull().actionsCleared()
        eventBus.dispatch()
        // Then
        actionsView.displayedActions.last().shouldBeEmpty()
    }

    @Test
    fun `StorySelected should list no action when another story is tapped in the sidebar`() {
        // Given
        startStorybook()
        storyContext.action("barTapped", 3)
        eventBus.dispatch()
        // When
        sidebarView.delegate.shouldNotBeNull().storySelected("screen-health-bar--empty")
        eventBus.dispatch()
        // Then
        actionsView.displayedActions.last().shouldBeEmpty()
    }
}
