package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRespawnBattleUnit
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.wandering.domain.WanderingRepository

/**
 * The party walked a step: every defeated CPU battle unit counts it, and those that have counted enough steps are
 * requested to respawn on their home. The ones whose home is occupied (e.g.: by a battle unit of the party) wait, and
 * are requested again on the next step.
 */
class RespawnDefeatedBattleUnits(
    private val wanderingRepository: WanderingRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val wandering = wanderingRepository.search()?.walkPartyStep() ?: return
        wanderingRepository.save(wandering)
        wandering
            .dueRespawns()
            .filter { home -> canBattlefieldTileBeOccupied(row = home.row, column = home.column) }
            .forEach { home -> eventBus.publish(RequestRespawnBattleUnit(battleUnitId = home.battleUnitId, row = home.row, column = home.column)) }
    }
}
