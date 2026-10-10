package com.mkz.rpg.screen.feedback

import com.mkz.rpg.screen.PixelGlyphs
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.image
import korlibs.math.geom.Size
import kotlin.math.abs
import kotlin.math.hypot

/** Builders for the procedural pixel views the feedback effects are made of. Everything is drawn in palette colors. */
object FxViews {
    const val FLASH_MS = 132
    const val NUMBER_MS = 480
    const val POOF_MS = 300
    const val POP_MS = 240
    const val NUMBER_RISE_ART_PIXELS = 4
    const val POOF_FRAMES = 3
    const val HIT_FRAMES = 3
    const val HEAL_FRAMES = 3
    const val HEAL_MS = 240
    const val VENOM_FRAMES = 3
    const val SPREAD_FRAMES = 2
    private const val TILE_ART_PIXELS = 16
    private val PLUS = listOf(" # ", "###", " # ")

    /** The sprite painted white, so a hit reads as a flash and not only as a color change. */
    fun flash(silhouette: Bitmap): Container =
        Container().also { container ->
            container.image(silhouette) {
                scale = PIXEL_SCALE.toDouble()
                smoothing = false
            }
        }

    /** White copy of [bitmap] that keeps its transparent pixels. */
    fun silhouette(bitmap: Bitmap): Bitmap {
        val result = Bitmap32(bitmap.width, bitmap.height)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val alpha = bitmap.getRgba(x, y).a
                if (alpha > 0) result[x, y] = UiPalette.textPrimary.withA(alpha)
            }
        }
        return result
    }

    /** A damage number, or a heal number led by a plus sign, so the two never differ by color alone. */
    fun amountNumber(
        amount: Int,
        heal: Boolean,
    ): Container {
        val color = if (heal) UiPalette.hpGain else UiPalette.hpLoss
        val number = PixelGlyphs.number(amount, color, PIXEL_SCALE, "${'$'}{NUMBER_NAME}_DIGITS")
        return Container().also { container ->
            container.name = NUMBER_NAME
            var x = 0
            if (heal) {
                container.addChild(
                    PixelGlyphs.glyph(PLUS, UiPalette.panel, PIXEL_SCALE).also {
                        it.x = (PIXEL_SCALE).toDouble()
                        it.y = (2 * PIXEL_SCALE).toDouble()
                    },
                )
                container.addChild(PixelGlyphs.glyph(PLUS, color, PIXEL_SCALE).also { it.y = PIXEL_SCALE.toDouble() })
                x = 4 * PIXEL_SCALE
            }
            number.x = x.toDouble()
            container.addChild(number)
        }
    }

    /** The art-pixel width of the number built by [amountNumber], used to center it on the tile. */
    fun amountWidthArtPixels(
        amount: Int,
        heal: Boolean,
    ): Int {
        val digits = amount.coerceIn(0, 99).toString().length
        return digits * 4 - 1 + if (heal) 4 else 0
    }

    /** An expanding ring of pixels. [frame] goes from 0 to [POOF_FRAMES] - 1. */
    fun poofFrame(frame: Int): Container {
        val radius = 2.0 + frame * 2.0
        val center = (TILE_ART_PIXELS - 1) / 2.0
        return Container().also { container ->
            for (y in 0 until TILE_ART_PIXELS) {
                for (x in 0 until TILE_ART_PIXELS) {
                    val distance = hypot(x - center, y - center)
                    if (abs(distance - radius) < 0.8) {
                        val color = if ((x + y) % 2 == 0) UiPalette.textPrimary else UiPalette.textMuted
                        container.addChild(artPixel(x, y, color))
                    }
                }
            }
        }
    }

    /** A plus-shaped sparkle for a status that just arrived. [frame] 0 is a dot, 1 the plus, 2 a wider plus. */
    fun popFrame(frame: Int): Container =
        Container().also { container ->
            val reach = frame.coerceIn(0, 2)
            container.addChild(artPixel(1, 1, UiPalette.conditional))
            for (step in 1..reach) {
                container.addChild(artPixel(1 - step, 1, UiPalette.conditional))
                container.addChild(artPixel(1 + step, 1, UiPalette.conditional))
                container.addChild(artPixel(1, 1 - step, UiPalette.conditional))
                container.addChild(artPixel(1, 1 + step, UiPalette.conditional))
            }
        }

    /** A 2x2 spark with a one-pixel trail, drawn at [x], [y] in points. */
    fun spark(): Container =
        Container().also { container ->
            container.addChild(artPixel(0, 0, UiPalette.conditional))
            container.addChild(artPixel(1, 0, UiPalette.textPrimary))
            container.addChild(artPixel(0, 1, UiPalette.textPrimary))
            container.addChild(artPixel(1, 1, UiPalette.textPrimary))
        }

    private fun artPixel(
        x: Int,
        y: Int,
        color: RGBA,
    ): SolidRect =
        SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), color).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
        }

    const val NUMBER_NAME = "FX_NUMBER"
    const val FLASH_NAME = "FX_FLASH"
    const val POOF_NAME = "FX_POOF"
    const val HIT_NAME = "FX_HIT"
    const val HEAL_NAME = "FX_HEAL"
    const val POP_NAME = "FX_POP"
    const val SPARK_NAME = "FX_SPARK"
    const val WALKER_NAME = "FX_WALKER"

    /** Whole steps for a progress 0..1 over [steps] steps, so motion stays on the art pixel grid. */
    fun steps(
        progress: Double,
        steps: Int,
    ): Int = (progress * steps).toInt().coerceIn(0, steps)
}
