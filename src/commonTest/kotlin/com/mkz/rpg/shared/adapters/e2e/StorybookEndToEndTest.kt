package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.screen.adapters.presentation.ShopView
import com.mkz.rpg.screen.adapters.presentation.action
import com.mkz.rpg.screen.adapters.presentation.clickBack
import com.mkz.rpg.screen.adapters.presentation.clickSlot
import com.mkz.rpg.screen.adapters.presentation.selectedSlotIndexes
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.startStorybook
import com.mkz.rpg.storybook.adapters.page.FakeStorybookPage
import com.mkz.rpg.storybook.adapters.presentation.actionTexts
import com.mkz.rpg.storybook.adapters.presentation.actionsView
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class StorybookEndToEndTest : ViewsForTesting(windowSize = STORYBOOK_WINDOW_SIZE) {
    private fun displayedHealthBarLabel(): String? = (stage.phoneFrameView()?.displayedView() as? HealthBarView)?.labelText()

    private fun displayedShopView(): ShopView? = stage.phoneFrameView()?.displayedView() as? ShopView

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
    fun `should tell the page once the story a link asks for has rendered, so a script can wait for it`() =
        sharedViewsTest {
            // Given
            val storybookPage = FakeStorybookPage()
            // When
            startStorybook(FakeStoryLocation(requestedStoryId = "screen-shop--sell"), storybookPage)
            // Then
            waitUntil { storybookPage.renderedStoryIds.isNotEmpty() }
            storybookPage.renderedStoryIds.shouldContainExactly("screen-shop--sell")
            displayedShopView().shouldNotBeNull()
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

    @Test
    fun `should log the callbacks of the view on show, and select the slot tapped through its arg, when the shop is tapped`() =
        sharedViewsTest {
            // Given
            val storyLocation = FakeStoryLocation(requestedStoryId = "screen-shop--buy")
            startStorybook(storyLocation)
            waitUntil { displayedShopView() != null }
            val shopView = requireNotNull(displayedShopView())
            // When
            clickSlot(shopView, 2)
            // Then
            waitUntil { displayedShopView()?.selectedSlotIndexes() == listOf(2) }
            displayedShopView()?.selectedSlotIndexes().shouldContainExactly(2)
            displayedShopView()?.action() shouldBe ("Buy" to true)
            stage.actionsView()?.actionTexts().shouldContainExactly("shopSlotSelected(2)")
            stage.controlsView()?.displayedValue("selectedSlot") shouldBe "2"
            storyLocation.keptLocations.last().args shouldBe mapOf("selectedSlot" to "2")
        }

    @Test
    fun `should only log a callback that changes no arg, the newest first, when Back is tapped`() =
        sharedViewsTest {
            // Given
            startStorybook(FakeStoryLocation(requestedStoryId = "screen-shop--buy-item-selected"))
            waitUntil { displayedShopView() != null }
            clickSlot(requireNotNull(displayedShopView()), 1)
            waitUntil { displayedShopView()?.selectedSlotIndexes()?.isEmpty() == true }
            // When
            clickBack(requireNotNull(displayedShopView()))
            // Then
            waitUntil { stage.actionsView()?.actionTexts()?.size == 2 }
            stage.actionsView()?.actionTexts().shouldContainExactly("shopBackSelected()", "shopSlotSelected(1)")
            displayedShopView()?.selectedSlotIndexes()?.shouldBeEmpty()
        }
}
