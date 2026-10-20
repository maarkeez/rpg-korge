package com.mkz.rpg.party.domain

object PartyMother {
    fun party(
        playerId: String = "human",
        battleUnitIds: List<String> = listOf("knight-1", "knight-2"),
    ): Party = Party.start(playerId = playerId, battleUnitIds = battleUnitIds).pullEvents().second
}
