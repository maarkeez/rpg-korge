package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MenuViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private fun newMenuView() = MenuView(onNewGameSelected = { selections += "new game" }, onMapBuilderSelected = { selections += "map builder" })

    @Test
    fun `MenuView should display playing a new game and building a map when it is displayed`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            // Then
            menuView.playNewGameLabel() shouldBe "Play new game"
            menuView.buildMapLabel() shouldBe "Build a map"
        }

    @Test
    fun `PlayNewGame should start a new game when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickPlayNewGame(menuView)
            // Then
            selections.shouldContainExactly("new game")
        }

    @Test
    fun `BuildMap should open the map builder when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickBuildMap(menuView)
            // Then
            selections.shouldContainExactly("map builder")
        }

    @Test
    fun `MenuView should display its options when it is displayed`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            addChild(menuView)
            // Then
            menuView.areOptionsShown() shouldBe true
        }

    @Test
    fun `PlayNewGame should hide the options when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickPlayNewGame(menuView)
            // Then
            menuView.areOptionsShown() shouldBe false
        }

    @Test
    fun `BuildMap should hide the options when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickBuildMap(menuView)
            // Then
            menuView.areOptionsShown() shouldBe false
        }

    @Test
    fun `MenuView should ignore other clicks once an option is selected`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickPlayNewGame(menuView)
            // When
            clickPlayNewGame(menuView)
            clickBuildMap(menuView)
            // Then
            selections.shouldContainExactly("new game")
        }
}
