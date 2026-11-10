package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

fun PillView.labelText(): String? = label?.text

fun PillView.labelColor(): RGBA? = label?.color

fun PillView.isGlyphDisplayed(): Boolean = glyphView != null

fun PillView.isGlyphBeforeLabel(): Boolean {
    val glyph = glyphView ?: return false
    val label = label ?: return false
    return glyph.x + glyph.width <= label.x
}
