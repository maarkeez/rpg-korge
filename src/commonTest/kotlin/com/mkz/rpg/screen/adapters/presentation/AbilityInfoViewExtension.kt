package com.mkz.rpg.screen.adapters.presentation

// Page object of [AbilityInfoView].

fun AbilityInfoView.abilityName(): String = nameText.text

/** The mana manaCost, as told by its pill. */
fun AbilityInfoView.manaCost(): String? = manaCostPill.labelText()

/** The cooldown, as told by its pill, or null when no cooldown pill is displayed. */
fun AbilityInfoView.cooldown(): String? = cooldownPill?.labelText()

/** Why the ability can not be cast now, e.g.: "Ready in 2 turns", or null when none is displayed. */
fun AbilityInfoView.reason(): String? = reasonText?.text

/** Whether the reason is displayed in a banner across the card, under its header. */
fun AbilityInfoView.isReasonInBanner(): Boolean {
    val banner = reasonBanner ?: return false
    val reason = reasonText ?: return false
    return reason.y >= banner.y && reason.y + reason.height <= banner.y + banner.height && banner.y >= nameText.y + nameText.height
}

fun AbilityInfoView.targeting(): String = targetingText.text

/** The sentence of each effect, its wrapped words joined back. */
fun AbilityInfoView.effects(): List<String> = effectWords.map { words -> words.joinToString(" ") { word -> word.text } }

/** The words of the effects drawn in gold, e.g.: their numbers. */
fun AbilityInfoView.highlightedWords(): List<String> = effectWords.flatten().filter { word -> word.color == HudStyle.SELECTION }.map { word -> word.text }

/** The four tiles orthogonally adjacent to the caster in the diagram: up, left, right and down. */
fun AbilityInfoView.adjacentMarks(): List<AbilityInfoView.Mark> = listOf(1, 3, 5, 7).map(marks::get)

fun AbilityInfoView.casterMark(): AbilityInfoView.Mark = marks[4]
