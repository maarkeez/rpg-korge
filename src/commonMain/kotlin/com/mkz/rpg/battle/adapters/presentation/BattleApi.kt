package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battle.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battle.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battle.adapters.events.OnPlayerDefeated
import com.mkz.rpg.battle.adapters.events.OnRequestFinishPlayerTurn
import com.mkz.rpg.battle.adapters.events.OnRequestInitialiseBattle
import com.mkz.rpg.battle.adapters.events.OnRoundFinished
import com.mkz.rpg.battle.adapters.resources.ResourcesBattleLoader
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.commands.InitialiseBattle
import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.battle.usecases.commands.LeaveBattle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battle.usecases.services.BattleLoader
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.shared.domain.EventBus

class BattleApi(
    eventBus: EventBus,
    battleUnitApi: BattleUnitApi,
    private val battleLoader: BattleLoader = ResourcesBattleLoader(),
) {
    // Storage
    private val battleRepository: BattleRepository = InMemoryBattleRepository()

    // Commands
    val defeatPlayer = DefeatPlayer(battleUnitApi.hasAllBattleUnitsDefeated, battleRepository, eventBus)
    val finishBattle = FinishBattle(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val initialiseBattle = InitialiseBattle(battleLoader, battleUnitApi.searchBattleUnitsByPlayerId, battleRepository, eventBus)
    val joinBattle = JoinBattle(battleRepository)
    val leaveBattle = LeaveBattle(battleRepository)
    val startNextRound = StartNextRound(battleRepository, eventBus)

    // Queries
    val searchBattle = SearchBattle(battleRepository)

    // Event Listeners
    val onTurnFinished = OnRoundFinished(eventBus, startNextRound)
    val onBattleUnitDeployed = OnBattleUnitDeployed(eventBus, joinBattle)
    val onBattleUnitDefeated = OnBattleUnitDefeated(eventBus, leaveBattle, defeatPlayer)
    val onPlayerDefeated = OnPlayerDefeated(eventBus, finishBattle)
    private val onRequestInitialiseBattle = OnRequestInitialiseBattle(initialiseBattle, eventBus)
    private val onRequestFinishPlayerTurn = OnRequestFinishPlayerTurn(finishPlayerTurn, eventBus)

    suspend fun init() {
        battleLoader.initResources()
    }
}
