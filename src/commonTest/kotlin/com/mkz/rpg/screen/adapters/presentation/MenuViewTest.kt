package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MenuViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private val texts = TextsMother.texts()

    private fun newMenuView() =
        MenuView(texts, onNewGameSelected = { selections += "new game" }, onLoadGameSelected = { selections += "load game" }, onMapBuilderSelected = { selections += "map builder" })

    @Test
    fun `MenuView should display playing a new game, building a map and the language when it is displayed`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            // Then
            menuView.playNewGameLabel() shouldBe "Play new game"
            menuView.loadGameLabel() shouldBe "Load game"
            menuView.buildMapLabel() shouldBe "Build a map"
            menuView.languageLabel() shouldBe "Language"
            menuView.areLanguagesShown() shouldBe false
        }

    @Test
    fun `Language should list the languages, named in their language, in place of the options when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickLanguage(menuView)
            // Then
            menuView.languageNames().shouldContainExactly("English", "Español")
            menuView.areLanguagesShown() shouldBe true
            menuView.areOptionsShown() shouldBe false
        }

    @Test
    fun `PickLanguage should translate the game and display the options relabelled when a language is picked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickLanguage(menuView)
            // When
            pickLanguage(menuView, "Español")
            // Then
            menuView.playNewGameLabel() shouldBe "Jugar partida nueva"
            menuView.loadGameLabel() shouldBe "Cargar partida"
            menuView.buildMapLabel() shouldBe "Construir un mapa"
            menuView.languageLabel() shouldBe "Idioma"
            menuView.areOptionsShown() shouldBe true
            menuView.areLanguagesShown() shouldBe false
            texts[UiText.BATTLE_INFO_EXPLORING] shouldBe "Explorando"
        }

    @Test
    fun `PickLanguage should translate the game back to English when English is picked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickLanguage(menuView)
            pickLanguage(menuView, "Español")
            clickLanguage(menuView)
            // When
            pickLanguage(menuView, "English")
            // Then
            menuView.playNewGameLabel() shouldBe "Play new game"
            texts[UiText.BATTLE_INFO_EXPLORING] shouldBe "Exploring"
        }

    @Test
    fun `PickLanguage should not start a game when a language is picked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickLanguage(menuView)
            // When
            pickLanguage(menuView, "Español")
            // Then
            selections.shouldBeEmpty()
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
    fun `LoadGame should list the save slots, hiding the options, when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickLoadGame(menuView)
            // Then
            selections.shouldContainExactly("load game")
            menuView.areOptionsShown() shouldBe false
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
