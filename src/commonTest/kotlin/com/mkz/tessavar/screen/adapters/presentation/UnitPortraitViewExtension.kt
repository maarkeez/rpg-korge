package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.RGBA

fun UnitPortraitView.isPortraitDisplayed(): Boolean = slot.findViewByName(UnitPortraitView.PORTRAIT) != null

fun UnitPortraitView.ringColor(): RGBA? = slot.findViewByName(UnitPortraitView.RING)?.let { ringColor }
