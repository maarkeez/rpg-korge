package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain

interface BattlefieldHudRepository {
    fun create(battlefieldHud: BattlefieldHud)

    fun update(battlefieldHud: BattlefieldHud)

    fun search(): BattlefieldHud?
}
