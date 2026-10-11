package com.mkz.rpg.screen.adapters.presentation

// Page object of [BattleInfoView].

/** The round text, or `null` when it is not displayed. */
fun BattleInfoView.roundText(): String? = round.takeIf { it.parent != null }?.text

/** The player turn text, or `null` when it is not displayed. */
fun BattleInfoView.playerTurnText(): String? = playerTurn.takeIf { it.parent != null }?.text

/** The winner text, or `null` when there is no winner yet. */
fun BattleInfoView.winnerText(): String? = winner?.text
