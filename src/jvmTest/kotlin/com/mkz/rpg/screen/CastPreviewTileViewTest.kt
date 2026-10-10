package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.view.SolidRect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CastPreviewTileViewTest {
    @Test
    fun `should draw corner brackets and side ticks when created`() {
        // Given / When
        val castPreviewTileView = CastPreviewTileView()
        // Then
        // 4 corners x (4 + 3 pixels) + 4 ticks x 2 pixels
        assertThat(castPreviewTileView.children).hasSize(36)
    }

    @Test
    fun `should only use the cast preview color when created`() {
        // Given / When
        val castPreviewTileView = CastPreviewTileView()
        // Then
        assertThat(castPreviewTileView.children.map { (it as SolidRect).color }.distinct()).containsExactly(UiPalette.castPreview)
    }
}
