package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment

fun UnitNameView.nameText(): String = unitNameLabel.text

fun UnitNameView.isAlignedToRight(): Boolean = unitNameLabel.styles.textAlignment == TextAlignment.RIGHT
