package com.mkz.rpg.screen.adapters.presentation

fun AbilityInfoView.abilityName(): String = nameText.text

fun AbilityInfoView.manaCost(): String? = manaCostPill.labelText()

fun AbilityInfoView.cooldown(): String? = cooldownPill?.labelText()

fun AbilityInfoView.reason(): String? = reasonText?.text

fun AbilityInfoView.isReasonInBanner(): Boolean {
    val banner = reasonBanner ?: return false
    val reason = reasonText ?: return false
    return reason.y >= banner.y && reason.y + reason.height <= banner.y + banner.height && banner.y >= nameText.y + nameText.height
}

fun AbilityInfoView.targeting(): String = targetingText.text

fun AbilityInfoView.effects(): List<String> = effectWords.map { words -> words.joinToString(" ") { word -> word.text } }

fun AbilityInfoView.highlightedWords(): List<String> = effectWords.flatten().filter { word -> word.color == HudStyle.SELECTION }.map { word -> word.text }

fun AbilityInfoView.adjacentMarks(): List<AbilityInfoView.Mark> = listOf(1, 3, 5, 7).map(marks::get)

fun AbilityInfoView.casterMark(): AbilityInfoView.Mark = marks[4]
