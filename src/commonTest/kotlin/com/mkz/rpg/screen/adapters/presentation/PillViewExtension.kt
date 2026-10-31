package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

// Page object of [PillView].

fun PillView.labelText(): String? = label?.text

fun PillView.labelColor(): RGBA? = label?.color

fun PillView.isGlyphDisplayed(): Boolean = glyphView != null

/** Whether the glyph is drawn before the text, from the left of the capsule. */
fun PillView.isGlyphBeforeLabel(): Boolean {
    val glyph = glyphView ?: return false
    val label = label ?: return false
    return glyph.x + glyph.width <= label.x
}
