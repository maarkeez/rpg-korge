package com.mkz.rpg.screen.adapters.presentation

// Page object of [UnitPortraitView].

fun UnitPortraitView.isPortraitDisplayed(): Boolean = battleUnitPortrait.findViewByName(UnitPortraitView.PORTRAIT) != null
