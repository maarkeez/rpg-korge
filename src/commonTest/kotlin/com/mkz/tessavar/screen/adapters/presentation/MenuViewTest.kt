package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MenuViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private val messages = MessagesMother.messages()

    private fun newMenuView() =
        MenuView(
            messages,
            onNewGameSelected = { selections += "new game" },
            onLoadGameSelected = { selections += "load game" },
            onWorldBuilderSelected = { selections += "world builder" },
            onPrivacySelected = { selections += "privacy" },
            onWishlistSelected = { selections += "wishlist" },
        )

    @Test
    fun `MenuView should display playing a new game, building a world and the language when it is displayed`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            // Then
            menuView.playNewGameLabel() shouldBe "Play new game"
            menuView.loadGameLabel() shouldBe "Load game"
            menuView.buildWorldLabel() shouldBe "Build a world"
            menuView.languageLabel() shouldBe "Language"
            menuView.privacyLabel() shouldBe "Privacy"
            menuView.wishlistLabel() shouldBe "Wishlist"
            menuView.areLanguagesShown() shouldBe false
        }

    @Test
    fun `MenuView should show Tessavar in gold above the buttons when it opens`() =
        sharedViewsTest {
            // When
            val menuView = newMenuView()
            // Then
            menuView.titleText() shouldBe "Tessavar"
            menuView.titleColor() shouldBe HudStyle.COIN_MIDDLE
            menuView.isTitleAboveTheOptions() shouldBe true
            menuView.isTitleCentred() shouldBe true
        }

    @Test
    fun `MenuView should keep showing Tessavar when the languages are listed`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickLanguage(menuView)
            // Then
            menuView.isTitleShown() shouldBe true
            menuView.titleText() shouldBe "Tessavar"
        }

    @Test
    fun `MenuView should keep Tessavar untranslated when Spanish is picked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            clickLanguage(menuView)
            // When
            pickLanguage(menuView, "Español")
            // Then
            menuView.titleText() shouldBe "Tessavar"
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
            menuView.buildWorldLabel() shouldBe "Construir un mundo"
            menuView.languageLabel() shouldBe "Idioma"
            menuView.privacyLabel() shouldBe "Privacidad"
            menuView.wishlistLabel() shouldBe "Lista de deseados"
            menuView.areOptionsShown() shouldBe true
            menuView.areLanguagesShown() shouldBe false
            messages[UiMessage.BANNER_EXPLORING] shouldBe "Explorando"
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
            messages[UiMessage.BANNER_EXPLORING] shouldBe "Exploring"
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
    fun `BuildWorld should open the world builder when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickBuildWorld(menuView)
            // Then
            selections.shouldContainExactly("world builder")
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
    fun `BuildWorld should hide the options when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickBuildWorld(menuView)
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
            clickBuildWorld(menuView)
            // Then
            selections.shouldContainExactly("new game")
        }

    @Test
    fun `MenuView should run onPrivacySelected when Privacy is tapped`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickPrivacy(menuView)
            // Then
            selections.shouldContainExactly("privacy")
        }

    @Test
    fun `Wishlist should ask to open the signup form when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickWishlist(menuView)
            // Then
            selections.shouldContainExactly("wishlist")
        }

    @Test
    fun `Wishlist should keep the options shown when it is clicked`() =
        sharedViewsTest {
            // Given
            val menuView = newMenuView()
            addChild(menuView)
            // When
            clickWishlist(menuView)
            // Then
            menuView.areOptionsShown() shouldBe true
        }
}
