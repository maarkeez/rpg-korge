package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.style.textColor

fun WorldBuilderInfoView.titleText(): String = title.text

fun WorldBuilderInfoView.subtitleText(): String = subtitle.text

fun WorldBuilderInfoView.subtitleColor(): RGBA = subtitle.styles.textColor

fun WorldBuilderInfoView.subtitleScale(): Double = subtitle.scaleX

fun WorldBuilderInfoView.isDisplayingMessage(): Boolean = messageAnimator != null
