package com.mkz.rpg.screen.feedback

import korlibs.korge.view.Container
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FxLayerTest {
    private val fxLayer = FxLayer()

    @Nested
    inner class Play {
        @Test
        fun `should show the view and report progress zero when an effect starts`() {
            // Given
            val effect = Container()
            val progress = mutableListOf<Double>()
            // When
            fxLayer.play(effect, durationMs = 100) { progress += it }
            // Then
            assertThat(effect.parent).isSameAs(fxLayer)
            assertThat(fxLayer.activeCount).isEqualTo(1)
            assertThat(progress).containsExactly(0.0)
        }

        @Test
        fun `should not intercept pointer input when effects are shown`() {
            // Given
            // When
            val mouseEnabled = fxLayer.mouseEnabled
            // Then
            assertThat(mouseEnabled).isFalse()
        }
    }

    @Nested
    inner class Advance {
        @Test
        fun `should report the elapsed fraction when part of the duration has passed`() {
            // Given
            val progress = mutableListOf<Double>()
            fxLayer.play(Container(), durationMs = 100) { progress += it }
            // When
            fxLayer.advance(deltaMs = 25.0)
            // Then
            assertThat(progress.last()).isEqualTo(0.25)
            assertThat(fxLayer.activeCount).isEqualTo(1)
        }

        @Test
        fun `should report full progress once and remove the view when the duration has passed`() {
            // Given
            val effect = Container()
            val progress = mutableListOf<Double>()
            fxLayer.play(effect, durationMs = 100) { progress += it }
            // When
            fxLayer.advance(deltaMs = 150.0)
            fxLayer.advance(deltaMs = 150.0)
            // Then
            assertThat(progress).containsExactly(0.0, 1.0)
            assertThat(effect.parent).isNull()
            assertThat(fxLayer.activeCount).isZero()
        }
    }

    @Nested
    inner class ClearEffects {
        @Test
        fun `should remove every effect when effects are cleared`() {
            // Given
            fxLayer.play(Container(), durationMs = 100)
            fxLayer.play(Container(), durationMs = 200)
            // When
            fxLayer.clearEffects()
            // Then
            assertThat(fxLayer.activeCount).isZero()
            assertThat(fxLayer.children).isEmpty()
        }
    }
}
