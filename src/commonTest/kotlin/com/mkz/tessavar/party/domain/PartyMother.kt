package com.mkz.tessavar.party.domain

object PartyMother {
    fun party(
        playerId: String = "human",
        battleUnitIds: List<String> = listOf("knight-1", "knight-2"),
        unitIds: List<String> = battleUnitIds.map(::unitIdOf),
    ): Party = Party.start(playerId = playerId, battleUnitIds = battleUnitIds, unitIds = unitIds).pullEvents().second

    private fun unitIdOf(battleUnitId: String): String = battleUnitId.substringBeforeLast("-")
}
