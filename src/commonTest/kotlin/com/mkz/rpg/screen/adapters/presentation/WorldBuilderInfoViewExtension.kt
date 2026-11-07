package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.style.textColor

// Page object of [WorldBuilderInfoView].

fun WorldBuilderInfoView.titleText(): String = title.text

/** The subtitle: the world size, or a message while it is displayed. */
fun WorldBuilderInfoView.subtitleText(): String = subtitle.text

fun WorldBuilderInfoView.subtitleColor(): RGBA = subtitle.styles.textColor

/** How big the subtitle is displayed: 1.0 is its normal size. */
fun WorldBuilderInfoView.subtitleScale(): Double = subtitle.scaleX

/** Whether a message is displayed instead of the world size. */
fun WorldBuilderInfoView.isDisplayingMessage(): Boolean = messageAnimator != null
