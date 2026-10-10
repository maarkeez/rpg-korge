package com.mkz.rpg.shared.adapters.presentation

import kotlin.math.roundToInt

/** Points drawn per art pixel. All pixel art is drawn at this integer scale. */
const val PIXEL_SCALE: Int = 3

/** Rounds [value] to an integer point. */
fun snap(value: Double): Double = value.roundToInt().toDouble()

/** Rounds [value] to a multiple of [PIXEL_SCALE]. */
fun snapToArtPixel(value: Double): Double = (value / PIXEL_SCALE).roundToInt().toDouble() * PIXEL_SCALE
