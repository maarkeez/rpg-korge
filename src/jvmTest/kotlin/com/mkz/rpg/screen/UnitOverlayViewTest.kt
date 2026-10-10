package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.view.SolidRect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnitOverlayViewTest {
    @Nested
    inner class Display {
        @Test
        fun `should expose the remaining health fraction when the overlay is displayed`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 25, maximumHealthPoints = 100))
            // Then
            assertThat(overlayView.hpFraction).isEqualTo(0.25)
        }

        @Test
        fun `should fill the bar proportionally when the unit is damaged`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 50, maximumHealthPoints = 100))
            // Then
            val fillWidths = overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_FILL }.map { it.width }
            assertThat(fillWidths).containsOnly(7.0 * PIXEL_SCALE)
        }

        @Test
        fun `should draw a full bar of 14 art pixels when the unit has full health`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 10, maximumHealthPoints = 10, isEnemy = false))
            // Then
            val fillWidths = overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_FILL }.map { it.width }
            assertThat(fillWidths).containsExactly(14.0 * PIXEL_SCALE, 14.0 * PIXEL_SCALE)
        }

        @Test
        fun `should keep at least one filled pixel when the unit is alive with almost no health`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 1, maximumHealthPoints = 100))
            // Then
            assertThat(overlayView.children.any { it.name == UnitOverlayView.HP_BAR_FILL }).isTrue()
        }

        @Test
        fun `should draw a flat track when the unit is an ally`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(isEnemy = false))
            // Then
            val tracks = overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_TRACK }
            assertThat(tracks.map { it.x }.distinct()).hasSize(1)
            assertThat(tracks.map { it.width }.distinct()).containsExactly(14.0 * PIXEL_SCALE)
        }

        @Test
        fun `should draw a notched track when the unit is an enemy`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(isEnemy = true))
            // Then
            val tracks = overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_TRACK }
            assertThat(tracks.map { it.width }).containsOnly(13.0 * PIXEL_SCALE)
            assertThat(tracks.map { it.x }.distinct()).hasSize(2)
        }

        @Test
        fun `should use a different fill color when the unit is an enemy`() {
            // Given
            val allyOverlay = UnitOverlayView()
            val enemyOverlay = UnitOverlayView()
            // When
            allyOverlay.display(overlayState(isEnemy = false))
            enemyOverlay.display(overlayState(isEnemy = true))
            // Then
            assertThat(fillColor(enemyOverlay)).isNotEqualTo(fillColor(allyOverlay))
        }

        @Test
        fun `should draw no pips when the unit has no statuses`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState())
            // Then
            assertThat(overlayView.visiblePipCount).isZero()
            assertThat(overlayView.hasOverflow).isFalse()
        }

        @Test
        fun `should draw one pip per status when the unit has up to two statuses`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(onTurnStartedEffectCount = 1, onDefeatedEffectCount = 1))
            // Then
            assertThat(overlayView.visiblePipCount).isEqualTo(2)
            assertThat(overlayView.hasOverflow).isFalse()
        }

        @Test
        fun `should cap the pips at two and show a plus when the unit has three statuses`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(onTurnStartedEffectCount = 3))
            // Then
            assertThat(overlayView.visiblePipCount).isEqualTo(2)
            assertThat(overlayView.hasOverflow).isTrue()
        }

        @Test
        fun `should keep the overlay inside the top and bottom three art pixels when the unit has statuses`() {
            // Given
            val overlayView = UnitOverlayView()
            val tileHeight = 16.0 * PIXEL_SCALE
            // When
            overlayView.display(overlayState(onTurnStartedEffectCount = 3, onDefeatedEffectCount = 1))
            // Then
            val artPixel = PIXEL_SCALE.toDouble()
            overlayView.children.forEach { child ->
                val bounds = child.getLocalBounds()
                val insideTop = bounds.bottom <= 3 * artPixel
                val insideBottom = bounds.top >= tileHeight - 3 * artPixel
                assertThat(insideTop || insideBottom).isTrue()
            }
        }
    }

    private fun fillColor(overlayView: UnitOverlayView) = (overlayView.children.first { it.name == UnitOverlayView.HP_BAR_FILL } as SolidRect).color

    @Nested
    inner class DisplayPreview {
        @Test
        fun `should draw a checkered ghost segment when the preview predicts damage`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 20, maximumHealthPoints = 20, preview = preview(hpAfter = 10)))
            // Then
            assertThat(overlayView.ghostPixelCount).isEqualTo(7)
            assertThat(overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_FILL }.map { it.width }).containsOnly(7.0 * PIXEL_SCALE)
        }

        @Test
        fun `should draw the ghost segment with the heal color when the preview predicts healing`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 10, maximumHealthPoints = 20, preview = preview(hpAfter = 20)))
            // Then
            val ghostColors = overlayView.children.filter { it.name == UnitOverlayView.HP_BAR_GHOST }.map { (it as SolidRect).color }
            assertThat(ghostColors).isNotEmpty().containsOnly(UiPalette.hpGain)
        }

        @Test
        fun `should draw no ghost segment when the preview doesn't change the health`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 20, maximumHealthPoints = 20, preview = preview(hpAfter = 20)))
            // Then
            assertThat(overlayView.ghostPixelCount).isZero()
        }

        @Test
        fun `should draw a skull when the preview predicts a lethal hit`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(remainingHealthPoints = 5, maximumHealthPoints = 20, preview = preview(hpAfter = 0, isLethal = true)))
            // Then
            assertThat(overlayView.hasSkull).isTrue()
        }

        @Test
        fun `should draw pending pips with a plus badge when the preview applies statuses`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState(preview = preview(hpAfter = 10, pendingOnTurnCount = 1, pendingOnDefeatCount = 1)))
            // Then
            assertThat(overlayView.hasPendingBadge).isTrue()
            assertThat(overlayView.pendingPipCount).isEqualTo(2)
        }

        @Test
        fun `should draw no preview marks when the overlay has no preview`() {
            // Given
            val overlayView = UnitOverlayView()
            // When
            overlayView.display(overlayState())
            // Then
            assertThat(overlayView.hasSkull).isFalse()
            assertThat(overlayView.hasPendingBadge).isFalse()
            assertThat(overlayView.ghostPixelCount).isZero()
        }
    }

    private fun preview(
        hpAfter: Int,
        isLethal: Boolean = false,
        pendingOnTurnCount: Int = 0,
        pendingOnDefeatCount: Int = 0,
    ) = UnitPreviewState(hpAfter = hpAfter, isLethal = isLethal, pendingOnTurnCount = pendingOnTurnCount, pendingOnDefeatCount = pendingOnDefeatCount)

    private fun overlayState(
        remainingHealthPoints: Int = 10,
        maximumHealthPoints: Int = 10,
        isEnemy: Boolean = false,
        onTurnStartedEffectCount: Int = 0,
        onDefeatedEffectCount: Int = 0,
        preview: UnitPreviewState? = null,
    ) = UnitOverlayState(
        remainingHealthPoints = remainingHealthPoints,
        maximumHealthPoints = maximumHealthPoints,
        isEnemy = isEnemy,
        onTurnStartedEffectCount = onTurnStartedEffectCount,
        onDefeatedEffectCount = onDefeatedEffectCount,
        preview = preview,
    )
}
