package com.mkz.rpg.screen.adapters.presentation

/**
 * What the npc dialog draws of an npc: its name and its line, already translated, and its portrait, which is the one of
 * its unit when it has one (a recruitable npc), or else its own, if any. Mapped from the domain by the presenter.
 */
data class NpcDialog(
    val npcId: String,
    val unitId: String?,
    val name: String,
    val line: String,
)
