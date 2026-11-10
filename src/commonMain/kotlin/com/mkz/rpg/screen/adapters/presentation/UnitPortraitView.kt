package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

class UnitPortraitView(
    size: Size,
    private val resourcesLoader: GameSceneResourcesLoader,
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

    internal var ringColor: RGBA? = null
        private set

    fun display(
        unitId: String,
        ring: RGBA? = null,
    ) {
        slot.findViewByName(PORTRAIT)?.removeFromParent()
        slot.findViewByName(RING)?.removeFromParent()
        if (ring != null) {
            slot.container {
                name = RING
                roundRect(this@UnitPortraitView.size, RectCorners(HudStyle.SLOT_RADIUS), fill = ring)
                roundRect(
                    Size(this@UnitPortraitView.width - 2 * RING_THICKNESS, this@UnitPortraitView.height - 2 * RING_THICKNESS),
                    RectCorners(HudStyle.SLOT_RADIUS - RING_THICKNESS),
                    fill = HudStyle.PANEL_RAISED,
                ) { xy(RING_THICKNESS, RING_THICKNESS) }
            }
        }
        resourcesLoader.bitmapOrNull("unit/${unitId}_portrait.png")?.let { avatarBitmap ->
            slot.image(avatarBitmap) {
                name = PORTRAIT
                smoothing = false
                scale = portraitScale
                centerOn(slot)
            }
        }
        ringColor = ring
        visible = true
    }

    fun hide() {
        visible = false
    }
}
