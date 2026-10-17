package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingRepository

/** Remembers the tile a CPU battle unit is deployed on as its home, which it wanders around while out of a battle. */
class ClaimHome(
    private val wanderingRepository: WanderingRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return
        val player = searchPlayerById(id = battleUnit.playerId) ?: return
        if (player.type != PlayerTypeDto.CPU) return
        val wandering = wanderingRepository.search() ?: Wandering.create()
        wanderingRepository.save(wandering.claimHome(battleUnitId = battleUnitId, row = row, column = column))
    }
}
