package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battle.adapters.events.OnCombatEngaged
import com.mkz.rpg.battle.adapters.events.OnEngagementUpdated
import com.mkz.rpg.battle.adapters.events.OnPlayerDefeated
import com.mkz.rpg.battle.adapters.events.OnRequestDefeatPlayer
import com.mkz.rpg.battle.adapters.events.OnRequestFinishPlayerTurn
import com.mkz.rpg.battle.adapters.events.OnRequestJoinBattle
import com.mkz.rpg.battle.adapters.events.OnRequestLeaveBattle
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
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus

class BattleApi(
    eventBus: EventBus,
    playerApi: PlayerApi,
) {
    // Storage
    private val battleRepository: BattleRepository = InMemoryBattleRepository()

    // Commands
    val defeatPlayer = DefeatPlayer(playerApi.searchEnemyPlayer, battleRepository, eventBus)
    val finishBattle = FinishBattle(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val startBattle = StartBattle(battleRepository, eventBus)
    val joinBattle = JoinBattle(battleRepository)
    val leaveBattle = LeaveBattle(battleRepository)
    val startNextRound = StartNextRound(battleRepository, eventBus)

    // Queries
    val searchBattle = SearchBattle(battleRepository)

    // Event Listeners
    val onEngagementUpdated = OnEngagementUpdated(eventBus, startNextRound)
    val onPlayerDefeated = OnPlayerDefeated(eventBus, finishBattle)
    private val onCombatEngaged = OnCombatEngaged(startBattle, eventBus)
    private val onRequestFinishPlayerTurn = OnRequestFinishPlayerTurn(finishPlayerTurn, eventBus)
    private val onRequestJoinBattle = OnRequestJoinBattle(joinBattle, eventBus)
    private val onRequestLeaveBattle = OnRequestLeaveBattle(leaveBattle, eventBus)
    private val onRequestDefeatPlayer = OnRequestDefeatPlayer(defeatPlayer, eventBus)
}
