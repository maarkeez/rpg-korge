package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.startStorybook
import com.mkz.rpg.storybook.adapters.presentation.clickControlButton
import com.mkz.rpg.storybook.adapters.presentation.clickLanguage
import com.mkz.rpg.storybook.adapters.presentation.clickStory
import com.mkz.rpg.storybook.adapters.presentation.controlsView
import com.mkz.rpg.storybook.adapters.presentation.displayedValue
import com.mkz.rpg.storybook.adapters.presentation.displayedView
import com.mkz.rpg.storybook.adapters.presentation.isViewCenteredOnScreen
import com.mkz.rpg.storybook.adapters.presentation.phoneFrameView
import com.mkz.rpg.storybook.adapters.presentation.selectedLanguageNames
import com.mkz.rpg.storybook.adapters.presentation.selectedStoryNames
import com.mkz.rpg.storybook.adapters.presentation.sidebarView
import com.mkz.rpg.storybook.adapters.presentation.storyInfoView
import com.mkz.rpg.storybook.adapters.presentation.storyName
import com.mkz.rpg.storybook.adapters.presentation.toolbarView
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class StorybookEndToEndTest : ViewsForTesting(windowSize = STORYBOOK_WINDOW_SIZE) {
    private fun displayedHealthBarLabel(): String? = (stage.phoneFrameView()?.displayedView() as? HealthBarView)?.labelText()

    @Test
    fun `should show the story a link asks for when the Storybook starts`() =
        sharedViewsTest {
            // When
            startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--half"))
            // Then
            waitUntil { displayedHealthBarLabel() != null }
            displayedHealthBarLabel() shouldBe "15 / 30"
            stage.sidebarView()?.selectedStoryNames().shouldContainExactly("Half")
            stage.storyInfoView()?.storyName() shouldBe "Half"
            stage.phoneFrameView()?.isViewCenteredOnScreen()?.shouldBeTrue()
        }

    @Test
    fun `should show a story when it is tapped in the sidebar`() =
        sharedViewsTest {
            // Given
            val storyLocation = FakeStoryLocation()
            startStorybook(storyLocation)
            waitUntil { displayedHealthBarLabel() != null }
            val sidebarView = requireNotNull(stage.sidebarView()) { "The sidebar is not displayed" }
            // When
            clickStory(sidebarView, "Empty")
            // Then
            waitUntil { displayedHealthBarLabel() == "0 / 30" }
            displayedHealthBarLabel() shouldBe "0 / 30"
            sidebarView.selectedStoryNames().shouldContainExactly("Empty")
            storyLocation.keptStoryIds.shouldContainExactly("screen-health-bar--empty")
        }

    @Test
    fun `should show the story with the args and language a link asks for when the Storybook starts`() =
        sharedViewsTest {
            // When
            startStorybook(FakeStoryLocation(requestedStoryId = "screen-health-bar--half", requestedArgs = mapOf("maximum" to "40"), requestedGlobals = mapOf("language" to "es")))
            // Then
            waitUntil { displayedHealthBarLabel() != null }
            displayedHealthBarLabel() shouldBe "15 / 40"
            stage.controlsView()?.displayedValue("maximum") shouldBe "40"
            stage.toolbarView()?.selectedLanguageNames().shouldContainExactly("Español")
        }

    @Test
    fun `should show the story again with the arg, and keep it in the link, when a control is edited`() =
        sharedViewsTest {
            // Given
            val storyLocation = FakeStoryLocation()
            startStorybook(storyLocation)
            waitUntil { displayedHealthBarLabel() != null }
            val controlsView = requireNotNull(stage.controlsView()) { "The controls are not displayed" }
            // When
            clickControlButton(controlsView, "remaining", "decrease")
            // Then
            waitUntil { displayedHealthBarLabel() == "29 / 30" }
            displayedHealthBarLabel() shouldBe "29 / 30"
            stage.controlsView()?.displayedValue("remaining") shouldBe "29"
            storyLocation.keptLocations.last().args shouldBe mapOf("remaining" to "29")
        }

    @Test
    fun `should keep the language in the link when a language is tapped in the toolbar`() =
        sharedViewsTest {
            // Given
            val storyLocation = FakeStoryLocation()
            startStorybook(storyLocation)
            waitUntil { displayedHealthBarLabel() != null }
            val toolbarView = requireNotNull(stage.toolbarView()) { "The toolbar is not displayed" }
            // When
            clickLanguage(toolbarView, "Español")
            // Then
            waitUntil { storyLocation.keptLocations.isNotEmpty() }
            toolbarView.selectedLanguageNames().shouldContainExactly("Español")
            storyLocation.keptLocations.last().globals shouldBe mapOf("language" to "es")
        }
}
