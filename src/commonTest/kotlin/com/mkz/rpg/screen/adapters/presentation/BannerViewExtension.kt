package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.style.textColor

fun BannerView.roundText(): String? = round.takeIf { it.parent != null }?.text

fun BannerView.playerTurnText(): String? = playerTurn.takeIf { it.parent != null }?.text

fun BannerView.winnerText(): String? = winner?.text

fun BannerView.playerTurnColor(): RGBA = playerTurn.styles.textColor

fun BannerView.roundScale(): Double = round.scaleX

fun BannerView.playerTurnScale(): Double = playerTurn.scaleX
