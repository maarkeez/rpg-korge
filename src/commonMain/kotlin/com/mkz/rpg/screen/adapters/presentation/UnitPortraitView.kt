package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * The portrait of a unit in a dark rounded slot, ringed with a colour when given, e.g.: its team's. Portrait scale: how
 * many screen pixels each pixel of the 32x32 portrait takes, a whole number so it stays sharp.
 */
class UnitPortraitView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val portraitScale: Double = 3.0,
) : UIContainer(size) {
    companion object {
        const val PORTRAIT = "PORTRAIT"
        const val RING = "RING"

        private const val RING_THICKNESS = 2.0
    }

    internal val slot =
        container {
            panel(size, fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS)
        }

    /** The colour of the ring displayed, or null when there is none. */
    internal var ringColor: RGBA? = null
        private set

    fun display(
        unitId: String,
        ring: RGBA? = null,
    ) {
        slot.findViewByName(PORTRAIT)?.removeFromParent()
        slot.findViewByName(RING)?.removeFromParent()
        resourcesLoader.bitmapOrNull("unit/${unitId}_portrait.png")?.let { avatarBitmap ->
            slot.image(avatarBitmap) {
                name = PORTRAIT
                smoothing = false
                scale = portraitScale
                centerOn(slot)
            }
        }
        if (ring != null) {
            // Inset by half its thickness, as a stroke is drawn half outside its shape, which cuts it.
            val inset = RING_THICKNESS / 2
            slot.roundRect(
                Size(width - RING_THICKNESS, height - RING_THICKNESS),
                RectCorners(HudStyle.SLOT_RADIUS - inset),
                fill = Colors.TRANSPARENT,
                stroke = ring,
                strokeThickness = RING_THICKNESS,
            ) {
                name = RING
                xy(inset, inset)
            }
        }
        ringColor = ring
        visible = true
    }

    fun hide() {
        visible = false
    }
}
