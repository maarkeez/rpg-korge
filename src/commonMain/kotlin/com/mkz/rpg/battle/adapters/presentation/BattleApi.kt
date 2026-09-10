package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battle.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battle.adapters.events.OnPlayerDefeated
import com.mkz.rpg.battle.adapters.events.OnRoundFinished
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.commands.StartFirstRound
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.shared.domain.EventBus

class BattleApi(
    eventBus: EventBus,
    battleUnitApi: BattleUnitApi,
) {
    // Storage
    private val battleRepository: BattleRepository = InMemoryBattleRepository()

    // Commands
    val defeatPlayer = DefeatPlayer(battleUnitApi.hasAllBattleUnitsDefeated, battleRepository, eventBus)
    val finishBattle = FinishBattle(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val startFirstRound = StartFirstRound(battleRepository, eventBus)
    val startNextRound = StartNextRound(battleRepository, eventBus)

    // Queries
    val searchBattle = SearchBattle(battleRepository)

    // Event Listeners
    val onTurnFinished = OnRoundFinished(eventBus, startNextRound)
    val onBattleUnitDefeated = OnBattleUnitDefeated(eventBus, defeatPlayer)
    val onPlayerDefeated = OnPlayerDefeated(eventBus, finishBattle)
}
