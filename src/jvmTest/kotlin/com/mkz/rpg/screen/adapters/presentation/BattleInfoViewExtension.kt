package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.style.textColor

// Page object of [BattleInfoView].

/** The round text, or `null` when it is not displayed. */
fun BattleInfoView.roundText(): String? = round.takeIf { it.parent != null }?.text

/** The player turn text, or `null` when it is not displayed. */
fun BattleInfoView.playerTurnText(): String? = playerTurn.takeIf { it.parent != null }?.text

/** The winner text, or `null` when there is no winner yet. */
fun BattleInfoView.winnerText(): String? = winner?.text

/** The colour of the player turn text. */
fun BattleInfoView.playerTurnColor(): RGBA = playerTurn.styles.textColor

/** How big the round text is displayed: 1.0 is its normal size. */
fun BattleInfoView.roundScale(): Double = round.scaleX

/** How big the player turn text is displayed: 1.0 is its normal size. */
fun BattleInfoView.playerTurnScale(): Double = playerTurn.scaleX
