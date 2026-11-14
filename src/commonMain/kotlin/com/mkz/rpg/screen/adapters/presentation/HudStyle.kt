package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.BarView
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.roundRect
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

object HudStyle {
    val PANEL = RGBA(0x17, 0x17, 0x25)

    val PANEL_RAISED = RGBA(0x22, 0x22, 0x36)

    val STROKE = RGBA(0x36, 0x36, 0x50)

    val TRACK = BarView.TRACK

    val MUTED = RGBA(0xA4, 0xA4, 0xBE)

    val FAINT = RGBA(0x6C, 0x6C, 0x88)

    val SELECTION = RGBA(0xFF, 0xD8, 0x4A)

    val ALLY = RGBA(0x5A, 0xD2, 0x5A)
    val ENEMY = RGBA(0xFF, 0x37, 0x5F)
    val DEFEATED = RGBA(0x8A, 0x8A, 0xA0)

    val COIN_MIDDLE = RGBA(0xC7, 0x8A, 0x1E)
    val COIN_PRESSED = RGBA(0xA6, 0x73, 0x1A)

    val DANGER = RGBA(211, 47, 47)
    val DANGER_PRESSED = RGBA(183, 35, 35)

    val HEALTH = RGBA(255, 55, 95)
    val MANA = RGBA(0, 145, 255)

    val GAIN = ALLY

    const val PANEL_RADIUS = 10.0
    const val SLOT_RADIUS = 8.0

    fun Container.panel(
        size: Size,
        fill: RGBA = PANEL,
        radius: Double = PANEL_RADIUS,
        stroke: RGBA = STROKE,
    ): RoundRect = roundRect(size, RectCorners(radius), fill = fill, stroke = stroke, strokeThickness = 1.0)
}
