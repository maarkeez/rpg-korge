package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MapBuilderInfoViewTest : ViewsForTesting() {
    private companion object {
        // Each test frame lasts 10ms: the pop and the time the message stays, plus a few frames.
        const val MESSAGE_FRAMES = 250
    }

    private suspend fun ViewsForTesting.sampleWhileDisplayingMessage(
        infoView: MapBuilderInfoView,
        sample: () -> Double,
    ): List<Double> {
        val samples = mutableListOf<Double>()
        repeat(MESSAGE_FRAMES) {
            if (!infoView.isDisplayingMessage()) return samples
            samples += sample()
            nextFrame()
        }
        return samples
    }

    @Test
    fun `DisplaySize should display the title and the map size when the size is displayed`() =
        sharedViewsTest {
            // Given
            val infoView = MapBuilderInfoView()
            // When
            infoView.displaySize(rows = 16, columns = 24)
            // Then
            infoView.titleText() shouldBe "Map Builder"
            infoView.subtitleText() shouldBe "16 x 24"
        }

    @Test
    fun `DisplayError should pop the error in the subtitle and then display the size again when an error is displayed`() =
        sharedViewsTest {
            // Given
            val infoView = MapBuilderInfoView()
            addChild(infoView)
            infoView.displaySize(rows = 16, columns = 16)
            // When
            infoView.displayError("Water can not be next to lava")
            val text = infoView.subtitleText()
            val color = infoView.subtitleColor()
            val scales = sampleWhileDisplayingMessage(infoView) { infoView.subtitleScale() }
            // Then
            text shouldBe "Water can not be next to lava"
            color shouldBe MapBuilderInfoView.ERROR_COLOR
            scales.max() shouldBe (MapBuilderInfoView.POP_SCALE plusOrMinus 0.05)
            infoView.isDisplayingMessage().shouldBeFalse()
            infoView.subtitleText() shouldBe "16 x 16"
            infoView.subtitleColor() shouldBe Colors.WHITE
            infoView.subtitleScale() shouldBe 1.0
        }

    @Test
    fun `DisplayInfo should display the information in the subtitle when information is displayed`() =
        sharedViewsTest {
            // Given
            val infoView = MapBuilderInfoView()
            addChild(infoView)
            // When
            infoView.displayInfo("Map copied to the clipboard")
            // Then
            infoView.subtitleText() shouldBe "Map copied to the clipboard"
            infoView.subtitleColor() shouldBe MapBuilderInfoView.INFO_COLOR
        }

    @Test
    fun `DisplaySize should keep the message and display the new size after it when the size changes while a message is displayed`() =
        sharedViewsTest {
            // Given
            val infoView = MapBuilderInfoView()
            addChild(infoView)
            infoView.displaySize(rows = 16, columns = 16)
            infoView.displayError("Water can not be next to lava")
            // When
            infoView.displaySize(rows = 8, columns = 16)
            val text = infoView.subtitleText()
            sampleWhileDisplayingMessage(infoView) { 0.0 }
            // Then
            text shouldBe "Water can not be next to lava"
            infoView.subtitleText() shouldBe "8 x 16"
        }

    @Test
    fun `DisplayError should replace the previous message when another message is displayed`() =
        sharedViewsTest {
            // Given
            val infoView = MapBuilderInfoView()
            addChild(infoView)
            infoView.displaySize(rows = 16, columns = 16)
            infoView.displayError("Water can not be next to lava")
            repeat(150) { nextFrame() }
            // When
            infoView.displayInfo("Map copied to the clipboard")
            repeat(100) { nextFrame() }
            // Then
            infoView.subtitleText() shouldBe "Map copied to the clipboard"
        }
}
