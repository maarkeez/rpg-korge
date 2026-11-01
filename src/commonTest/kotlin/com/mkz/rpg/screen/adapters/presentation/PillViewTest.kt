package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PillViewTest : ViewsForTesting() {
    @Test
    fun `PillView should display the text in its colour when it is displayed`() =
        sharedViewsTest {
            // Given
            val pillView = PillView(color = HudStyle.MANA)
            // When
            pillView.display("10")
            // Then
            pillView.isVisibleToUser().shouldBeTrue()
            pillView.labelText() shouldBe "10"
            pillView.labelColor() shouldBe HudStyle.MANA
            pillView.isGlyphDisplayed().shouldBeFalse()
        }

    @Test
    fun `PillView should draw the mana orb before the text when its glyph is mana`() =
        sharedViewsTest {
            // Given
            val pillView = PillView(color = HudStyle.MANA, glyph = PillView.Glyph.MANA)
            // When
            pillView.display("10")
            // Then
            pillView.isGlyphDisplayed().shouldBeTrue()
            pillView.isGlyphBeforeLabel().shouldBeTrue()
        }

    @Test
    fun `PillView should draw the coin before the text when its glyph is gold`() =
        sharedViewsTest {
            // Given
            val pillView = PillView(color = HudStyle.SELECTION, glyph = PillView.Glyph.GOLD)
            // When
            pillView.display("150 G")
            // Then
            pillView.isGlyphDisplayed().shouldBeTrue()
            pillView.isGlyphBeforeLabel().shouldBeTrue()
        }

    @Test
    fun `PillView should display the text in white when it is solid`() =
        sharedViewsTest {
            // Given
            val pillView = PillView(color = HudStyle.HEALTH, solid = true)
            // When
            pillView.display("KO")
            // Then
            pillView.labelColor() shouldBe Colors.WHITE
        }

    @Test
    fun `PillView should not be visible when it is hidden`() =
        sharedViewsTest {
            // Given
            val pillView = PillView(color = HudStyle.MANA)
            pillView.display("10")
            // When
            pillView.hide()
            // Then
            pillView.isVisibleToUser().shouldBeFalse()
        }
}
