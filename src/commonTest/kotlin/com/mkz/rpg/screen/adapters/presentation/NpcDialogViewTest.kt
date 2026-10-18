package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class NpcDialogViewTest : ViewsForTesting() {
    private var taps = 0
    private val theGuy = npc(id = "the-guy", name = "The Guy", line = "G'day bloke!").toDto()

    private suspend fun newNpcDialogView() = NpcDialogView(Size(390, 300), loadedResourcesLoader()).also { dialog -> dialog.setOnTapped { taps++ } }

    @Test
    fun `NpcDialogView should be hidden when is created`() =
        sharedViewsTest {
            // When
            val dialog = newNpcDialogView()
            // Then
            dialog.isDialogShown().shouldBeFalse()
        }

    @Test
    fun `Display should display the name of the npc on top of its line, and its portrait on the left, at 3x`() =
        sharedViewsTest {
            // Given
            val dialog = newNpcDialogView()
            // When
            dialog.display(theGuy)
            // Then
            dialog.displayedName() shouldBe "The Guy"
            dialog.displayedLine() shouldBe "G'day bloke!"
            dialog.isNameAboveLine().shouldBeTrue()
            dialog.isPortraitLeftOfText().shouldBeTrue()
            dialog.portraitSize() shouldBe (96.0 to 96.0)
        }

    @Test
    fun `Display should leave the portrait out when the npc has none`() =
        sharedViewsTest {
            // Given
            val dialog = newNpcDialogView()
            // When
            dialog.display(npc(id = "the-ghost").toDto())
            // Then
            dialog.hasPortrait().shouldBeFalse()
        }

    @Test
    fun `Display should replace the npc displayed before`() =
        sharedViewsTest {
            // Given
            val dialog = newNpcDialogView()
            dialog.display(theGuy)
            // When
            dialog.display(npc(id = "the-girl", name = "The Girl", line = "Hi!").toDto())
            // Then
            dialog.displayedName() shouldBe "The Girl"
            dialog.displayedLine() shouldBe "Hi!"
            dialog.hasPortrait().shouldBeFalse()
        }

    @Test
    fun `ClickDialog should call the listener when the dialog is tapped`() =
        sharedViewsTest {
            // Given
            val dialog = newNpcDialogView()
            dialog.display(theGuy)
            dialog.visible = true
            addChild(dialog)
            // When
            clickDialog(dialog)
            // Then
            taps shouldBe 1
        }
}
