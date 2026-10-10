package com.mkz.rpg.shared.adapters.presentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PixelScaleTest {
    @Nested
    inner class Snap {
        @Test
        fun `should round to an integer point when the value is fractional`() {
            // Given
            val fractionalValue = 10.6
            // When
            val snapped = snap(fractionalValue)
            // Then
            assertThat(snapped).isEqualTo(11.0)
        }
    }

    @Nested
    inner class SnapToArtPixel {
        @Test
        fun `should round to a multiple of the pixel scale when the value is between art pixels`() {
            // Given
            val betweenArtPixels = 10.0
            // When
            val snapped = snapToArtPixel(betweenArtPixels)
            // Then
            assertThat(snapped).isEqualTo(9.0)
        }

        @Test
        fun `should keep the value when it is already a multiple of the pixel scale`() {
            // Given
            val alignedValue = 48.0
            // When
            val snapped = snapToArtPixel(alignedValue)
            // Then
            assertThat(snapped).isEqualTo(48.0)
        }
    }
}
