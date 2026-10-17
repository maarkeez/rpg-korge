package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MapBuilderMenuViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private fun newMenuView() =
        MapBuilderMenuView().apply {
            displayOptions(listOf(MapBuilderMenuView.Option("Import") { selections += "Import" }, MapBuilderMenuView.Option("Help") { selections += "Help" }))
            x = 300.0
            y = 10.0
        }

    @Test
    fun `MapBuilderMenuView should be closed when it is displayed`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            // Then
            menuView.isOpen.shouldBeFalse()
            menuView.optionLabels().shouldBeEmpty()
        }

    @Test
    fun `MenuButton should open the menu with its options when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickMenuButton(menuView)
            // Then
            menuView.isOpen.shouldBeTrue()
            menuView.optionLabels().shouldContainExactly("Import", "Help")
        }

    @Test
    fun `MenuButton should close the menu when it is clicked while the menu is open`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickMenuButton(menuView)
            // When
            clickMenuButton(menuView)
            // Then
            menuView.isOpen.shouldBeFalse()
        }

    @Test
    fun `MenuOption should close the menu and select the option when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickMenuButton(menuView)
            // When
            clickMenuOption(menuView, "Help")
            // Then
            selections.shouldContainExactly("Help")
            menuView.isOpen.shouldBeFalse()
        }

    @Test
    fun `MapBuilderMenuView should close the menu without selecting an option when the screen is clicked out of it`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickMenuButton(menuView)
            // When
            clickOutOfMenu(x = 50.0, y = 400.0)
            // Then
            menuView.isOpen.shouldBeFalse()
            selections.shouldBeEmpty()
        }
}
