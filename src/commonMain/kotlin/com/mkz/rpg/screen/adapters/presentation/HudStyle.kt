package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.BarView
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.roundRect
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * The palette and the panel every view of the battle HUD draws with. The HUD keeps pixel art for what the artist draws
 * (the portraits, the ability and effect icons, the chest), always at a whole scale, and draws the chrome around it as
 * vectors: rounded panels and slots, bars, pips, pills and rings.
 */
object HudStyle {
    // The panels: the header of the battle unit info, the ability info, the forecast rows.
    val PANEL = RGBA(0x17, 0x17, 0x25)

    // The slots: the portraits, the ability buttons, the bag, the effects.
    val PANEL_RAISED = RGBA(0x22, 0x22, 0x36)

    // The 1-pixel border of the panels and the slots.
    val STROKE = RGBA(0x36, 0x36, 0x50)

    // The empty part of the bars.
    val TRACK = BarView.TRACK

    // The secondary texts, e.g.: the targeting of an ability.
    val MUTED = RGBA(0xA4, 0xA4, 0xBE)

    // The captions, e.g.: MOVE, CAST, HP, MP.
    val FAINT = RGBA(0x6C, 0x6C, 0x88)

    // The selected ring, the numbers in the effects of an ability.
    val SELECTION = RGBA(0xFF, 0xD8, 0x4A)

    // The team rings of the portraits: the human player's battle units, the CPU's, and a receiver a cast would defeat.
    val ALLY = RGBA(0x5A, 0xD2, 0x5A)
    val ENEMY = RGBA(0xFF, 0x37, 0x5F)
    val DEFEATED = RGBA(0x8A, 0x8A, 0xA0)

    // The middle of a gold coin.
    val COIN_MIDDLE = RGBA(0xC7, 0x8A, 0x1E)

    val HEALTH = RGBA(255, 55, 95)
    val MANA = RGBA(0, 145, 255)

    // A gain previewed, e.g.: the health a heal would restore.
    val GAIN = ALLY

    const val PANEL_RADIUS = 10.0
    const val SLOT_RADIUS = 8.0

    /** A rounded rectangle with the [STROKE] border, e.g.: the header of the battle unit info, or a slot. */
    fun Container.panel(
        size: Size,
        fill: RGBA = PANEL,
        radius: Double = PANEL_RADIUS,
        stroke: RGBA = STROKE,
    ): RoundRect = roundRect(size, RectCorners(radius), fill = fill, stroke = stroke, strokeThickness = 1.0)
}
