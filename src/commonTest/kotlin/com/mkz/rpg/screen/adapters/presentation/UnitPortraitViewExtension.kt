package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

// Page object of [UnitPortraitView].

fun UnitPortraitView.isPortraitDisplayed(): Boolean = slot.findViewByName(UnitPortraitView.PORTRAIT) != null

/** The colour of the ring around the portrait, or null when it has none. */
fun UnitPortraitView.ringColor(): RGBA? = slot.findViewByName(UnitPortraitView.RING)?.let { ringColor }
