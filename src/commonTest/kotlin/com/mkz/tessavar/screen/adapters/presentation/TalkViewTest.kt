package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class TalkViewTest : ViewsForTesting() {
    private var taps = 0
    private val elder = TalkInfo(npcId = "elder", unitId = null, name = "Elder", line = "Long ago a goat chewed up a wizard's spellbook and swallowed its magic.")

    private suspend fun newTalkView() = TalkView(Size(390, 300), loadedResourcesLoader()).also { dialog -> dialog.setOnTapped { taps++ } }

    @Test
    fun `TalkView should be hidden when is created`() =
        sharedViewsTest {
            // When
            val dialog = newTalkView()
            // Then
            dialog.isDialogShown().shouldBeFalse()
        }

    @Test
    fun `Display should display the name of the npc on top of its line, and its portrait on the left, at 3x`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            // When
            dialog.display(elder)
            // Then
            dialog.displayedName() shouldBe "Elder"
            dialog.displayedLine() shouldBe "Long ago a goat chewed up a wizard's spellbook and swallowed its magic."
            dialog.isNameAboveLine().shouldBeTrue()
            dialog.isPortraitLeftOfText().shouldBeTrue()
            dialog.portraitSize() shouldBe (96.0 to 96.0)
        }

    @Test
    fun `Display should display the portrait of the unit of a recruitable npc`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            // When
            dialog.display(TalkInfo(npcId = "a-knight-to-recruit", unitId = "knight", name = "Knight", line = "Take me with you."))
            // Then
            dialog.portraitSize() shouldBe (96.0 to 96.0)
        }

    @Test
    fun `Display should leave the portrait out when the npc has none`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            // When
            dialog.display(TalkInfo(npcId = "the-ghost", unitId = null, name = "Ghost", line = "Boo."))
            // Then
            dialog.hasPortrait().shouldBeFalse()
        }

    @Test
    fun `Display should replace the npc displayed before`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            dialog.display(elder)
            // When
            dialog.display(TalkInfo(npcId = "herbalist", unitId = null, name = "Herbalist", line = "Herbs to heal, venom to kill."))
            // Then
            dialog.displayedName() shouldBe "Herbalist"
            dialog.displayedLine() shouldBe "Herbs to heal, venom to kill."
            dialog.hasPortrait().shouldBeFalse()
        }

    @Test
    fun `ClickDialog should call the listener when the dialog is tapped`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            dialog.display(elder)
            dialog.visible = true
            addChild(dialog)
            // When
            clickDialog(dialog)
            // Then
            taps shouldBe 1
        }

    @Test
    fun `Display should give the line the room of the choices`() =
        sharedViewsTest {
            // Given
            val dialog = newTalkView()
            // When
            dialog.display(elder)
            // Then
            dialog.lineHeight() shouldBe 236.0
        }
}
