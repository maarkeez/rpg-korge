package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.circle
import korlibs.korge.view.container
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

class PillView(
    private val color: RGBA,
    private val glyph: Glyph = Glyph.NONE,
    private val solid: Boolean = false,
    private val textSize: Double = TEXT_SIZE,
) : Container() {
    companion object {
        const val CAPSULE = "CAPSULE"
        const val LABEL = "LABEL"
        const val GLYPH = "GLYPH"

        const val TEXT_SIZE = 12.0
        private const val PADDING = 7.0
        private const val TINT_ALPHA = 0.16
        private const val STROKE_ALPHA = 0.45
        private const val GLYPH_SPACING = 2.0
    }

    enum class Glyph {
        NONE,

        MANA,

        COOLDOWN,

        GOLD,
    }

    internal var capsule: RoundRect? = null
    internal var label: Text? = null
    internal var glyphView: Container? = null

    val pillHeight: Double get() = textSize + 8

    fun display(text: String) {
        removeChildren()
        val pill = this
        val height = pillHeight
        val glyphWidth = if (glyph == Glyph.NONE) 0.0 else textSize + GLYPH_SPACING
        val label = Text(text, textSize = textSize, color = if (solid) Colors.WHITE else color).apply { name = LABEL }
        val width = label.width + PADDING * 2 + glyphWidth
        capsule =
            roundRect(
                Size(width, height),
                RectCorners(height / 2),
                fill = if (solid) color else color.withAd(TINT_ALPHA),
                stroke = if (solid) color else color.withAd(STROKE_ALPHA),
                strokeThickness = 1.0,
            ) { name = CAPSULE }
        glyphView =
            if (glyph == Glyph.NONE) {
                null
            } else {
                container {
                    name = GLYPH
                    drawGlyph(textSize)
                    xy(PADDING, (height - textSize) / 2)
                }
            }
        pill.addChild(label)
        label.xy(PADDING + glyphWidth, (height - label.height) / 2 + 0.5)
        this.label = label
        visible = true
    }

    fun hide() {
        visible = false
    }

    private fun Container.drawGlyph(side: Double) {
        val radius = side / 2 - 1
        val ink = if (solid) Colors.WHITE else color
        when (glyph) {
            Glyph.MANA -> {
                val orb = if (solid) Colors.WHITE else HudStyle.MANA
                val highlight = if (solid) HudStyle.MANA else Colors.WHITE
                circle(radius = radius, fill = orb) { xy(1.0, 1.0) }
                circle(radius = radius * 0.32, fill = highlight) { xy(1.0 + radius * 0.45, 1.0 + radius * 0.4) }
            }
            Glyph.COOLDOWN -> {
                circle(radius = radius, fill = Colors.TRANSPARENT, stroke = ink, strokeThickness = 1.5) { xy(1.0, 1.0) }
                solidRect(Size(1.5, side * 0.32), ink) { xy(side / 2 - 0.75, side / 2 - side * 0.3) }
                solidRect(Size(side * 0.26, 1.5), ink) { xy(side / 2 - 0.75, side / 2 - 0.75) }
            }
            Glyph.GOLD -> {
                circle(radius = radius, fill = HudStyle.SELECTION) { xy(1.0, 1.0) }
                circle(radius = radius * 0.5, fill = HudStyle.COIN_MIDDLE) { xy(1.0 + radius * 0.5, 1.0 + radius * 0.5) }
            }
            Glyph.NONE -> Unit
        }
    }
}
