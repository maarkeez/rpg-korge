package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.view.SolidRect

private fun TileDimView.mask(): SolidRect = children.single() as SolidRect

fun TileDimView.maskColor(): RGBA = mask().color

fun TileDimView.maskSize(): Pair<Double, Double> = mask().width to mask().height

fun TileDimView.maskAlpha(): Double = alpha
