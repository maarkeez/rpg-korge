package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.services.BattleLoader
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.shared.domain.EventBus

/** Starts the first round of the battle, with the players playing in the loaded order, and all their battle units in the battle. */
class InitialiseBattle(
    private val battleLoader: BattleLoader,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        if (battleRepository.search() != null) return
        val players = battleLoader.loadPlayerOrder()
        val battleUnitIds = players.flatMap { playerId -> searchBattleUnitsByPlayerId(playerId).map { battleUnit -> battleUnit.id } }.toSet()
        val (events, battle) = Battle.startFirstRound(players, battleUnitIds).pullEvents()
        battleRepository.create(battle)
        eventBus.publish(events)
    }
}
