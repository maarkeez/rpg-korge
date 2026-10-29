package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.view.SolidRect

// Page object of [TileDimView].

private fun TileDimView.mask(): SolidRect = children.single() as SolidRect

fun TileDimView.maskColor(): RGBA = mask().color

/** The size of the mask, as (width, height). */
fun TileDimView.maskSize(): Pair<Double, Double> = mask().width to mask().height

/** How opaque the mask is: 0.0 would not darken what is under it. */
fun TileDimView.maskAlpha(): Double = alpha
