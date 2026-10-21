package com.mkz.rpg.party.domain

object PartyMother {
    fun party(
        playerId: String = "human",
        battleUnitIds: List<String> = listOf("knight-1", "knight-2"),
        // The unit of each battle unit, by default its id without the number, e.g.: "knight" for "knight-1".
        unitIds: List<String> = battleUnitIds.map { battleUnitId -> battleUnitId.substringBeforeLast("-") },
    ): Party = Party.start(playerId = playerId, battleUnitIds = battleUnitIds, unitIds = unitIds).pullEvents().second
}
