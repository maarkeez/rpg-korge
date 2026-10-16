package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.style.textColor

// Page object of [MapBuilderInfoView].

fun MapBuilderInfoView.titleText(): String = title.text

/** The subtitle: the map size, or a message while it is displayed. */
fun MapBuilderInfoView.subtitleText(): String = subtitle.text

fun MapBuilderInfoView.subtitleColor(): RGBA = subtitle.styles.textColor

/** How big the subtitle is displayed: 1.0 is its normal size. */
fun MapBuilderInfoView.subtitleScale(): Double = subtitle.scaleX

/** Whether a message is displayed instead of the map size. */
fun MapBuilderInfoView.isDisplayingMessage(): Boolean = messageAnimator != null
