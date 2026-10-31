package com.mkz.rpg.screen.adapters.presentation

// Page object of [AbilityInfoView].

fun AbilityInfoView.abilityName(): String = nameText.text

fun AbilityInfoView.manaCost(): String = manaCostText.text

fun AbilityInfoView.cooldown(): String = cooldownText.text

/** Why the ability can not be cast now, e.g.: "Ready in 2 turns", or null when none is displayed. */
fun AbilityInfoView.reason(): String? = reasonText?.text

fun AbilityInfoView.targeting(): String = targetingLines.joinToString(" ") { line -> line.text }

/** The sentence of each effect, its wrapped lines joined back. */
fun AbilityInfoView.effects(): List<String> = effectLines.map { lines -> lines.joinToString(" ") { line -> line.text } }

/** The four tiles orthogonally adjacent to the caster in the diagram: up, left, right and down. */
fun AbilityInfoView.adjacentMarks(): List<AbilityInfoView.Mark> = listOf(1, 3, 5, 7).map(marks::get)

fun AbilityInfoView.casterMark(): AbilityInfoView.Mark = marks[4]
