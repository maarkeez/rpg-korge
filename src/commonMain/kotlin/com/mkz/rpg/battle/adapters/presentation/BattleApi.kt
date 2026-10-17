package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battle.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battle.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battle.adapters.events.OnCombatEngaged
import com.mkz.rpg.battle.adapters.events.OnEngagementUpdated
import com.mkz.rpg.battle.adapters.events.OnPlayerDefeated
import com.mkz.rpg.battle.adapters.events.OnRequestFinishPlayerTurn
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.battle.usecases.commands.LeaveBattle
import com.mkz.rpg.battle.usecases.commands.StartBattle
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
    val startBattle = StartBattle(battleUnitApi.searchBattleUnitsByPlayerId, battleRepository, eventBus)
    val joinBattle = JoinBattle(battleRepository)
    val leaveBattle = LeaveBattle(battleRepository)
    val startNextRound = StartNextRound(battleUnitApi.searchBattleUnitById, battleRepository, eventBus)

    // Queries
    val searchBattle = SearchBattle(battleRepository)

    // Event Listeners
    val onEngagementUpdated = OnEngagementUpdated(eventBus, startNextRound)
    val onBattleUnitDeployed = OnBattleUnitDeployed(eventBus, joinBattle)
    val onBattleUnitDefeated = OnBattleUnitDefeated(eventBus, leaveBattle, defeatPlayer)
    val onPlayerDefeated = OnPlayerDefeated(eventBus, finishBattle)
    private val onCombatEngaged = OnCombatEngaged(startBattle, eventBus)
    private val onRequestFinishPlayerTurn = OnRequestFinishPlayerTurn(finishPlayerTurn, eventBus)
}
