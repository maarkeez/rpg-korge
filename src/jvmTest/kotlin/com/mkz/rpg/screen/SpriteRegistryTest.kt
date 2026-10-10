package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpriteRegistryTest : ViewsForTesting() {
    private val spriteRegistry = SpriteRegistry()

    @Nested
    inner class Effect {
        @Test
        fun `should return a placeholder when the effect id is unknown`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val bitmap = spriteRegistry.effect("unknown-effect")
                // Then
                assertThat(bitmap.toBMP32()[0, 0]).isEqualTo(UiPalette.placeholder)
            }

        @Test
        fun `should return the effect sprite when the effect id is known`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val bitmap = spriteRegistry.effect("venom-damage")
                // Then
                assertThat(bitmap).isNotEqualTo(spriteRegistry.effect("unknown-effect"))
            }
    }

    @Nested
    inner class Unit {
        @Test
        fun `should return a placeholder when the unit id is unknown`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val bitmap = spriteRegistry.unit("dragon")
                // Then
                assertThat(bitmap.toBMP32()[0, 0]).isEqualTo(UiPalette.placeholder)
            }
    }

    @Nested
    inner class Ability {
        @Test
        fun `should return a placeholder when the ability id is unknown`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val bitmap = spriteRegistry.ability("fireball")
                // Then
                assertThat(bitmap.toBMP32()[0, 0]).isEqualTo(UiPalette.placeholder)
            }
    }

    @Nested
    inner class FxFrames {
        @Test
        fun `should return no frames when the fx strip does not exist`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val frames = spriteRegistry.fxFrames("fx_unknown")
                // Then
                assertThat(frames).isNull()
            }
    }

    @Nested
    inner class UnitFrames {
        @Test
        fun `should return every frame of the strip when the unit has an animation strip`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val frames = spriteRegistry.unitFrames("knight", SpriteRegistry.IDLE)
                // Then
                assertThat(frames).hasSize(2)
                assertThat(frames.map { it.width to it.height }).containsOnly(16 to 16)
            }

        @Test
        fun `should return the single unit sprite when the unit has no animation strip`() =
            viewsTest {
                // Given
                spriteRegistry.load()
                // When
                val frames = spriteRegistry.unitFrames("dragon", SpriteRegistry.IDLE)
                // Then
                assertThat(frames).hasSize(1)
                assertThat(frames.single().toBMP32()[0, 0]).isEqualTo(UiPalette.placeholder)
            }
    }
}
