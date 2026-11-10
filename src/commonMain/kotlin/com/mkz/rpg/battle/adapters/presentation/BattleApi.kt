package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battle.adapters.events.OnPlayerDefeated
import com.mkz.rpg.battle.adapters.events.OnRequestDefeatPlayer
import com.mkz.rpg.battle.adapters.events.OnRequestFinishPlayerTurn
import com.mkz.rpg.battle.adapters.events.OnRequestJoinBattle
import com.mkz.rpg.battle.adapters.events.OnRequestLeaveBattle
import com.mkz.rpg.battle.adapters.events.OnRequestResolveDisengagedBattle
import com.mkz.rpg.battle.adapters.events.OnRequestStartBattle
import com.mkz.rpg.battle.adapters.events.OnRequestStartNextRound
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battle.usecases.commands.FinishBattleWithWinner
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.battle.usecases.commands.LeaveBattle
import com.mkz.rpg.battle.usecases.commands.ResolveDisengagedBattle
import com.mkz.rpg.battle.usecases.commands.StartBattle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus

class BattleApi(
    eventBus: EventBus,
    playerApi: PlayerApi,
) {
    private val battleRepository: BattleRepository = InMemoryBattleRepository()

    val defeatPlayer = DefeatPlayer(playerApi.searchOpponent, battleRepository, eventBus)
    val finishBattleWithWinner = FinishBattleWithWinner(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val startBattle = StartBattle(battleRepository, eventBus)
    val joinBattle = JoinBattle(battleRepository)
    val leaveBattle = LeaveBattle(battleRepository, eventBus)
    val startNextRound = StartNextRound(battleRepository, eventBus)
    val resolveDisengagedBattle = ResolveDisengagedBattle(battleRepository, eventBus)

    val searchBattle = SearchBattle(battleRepository)

    val onRequestStartNextRound = OnRequestStartNextRound(eventBus, startNextRound)
    val onPlayerDefeated = OnPlayerDefeated(eventBus, finishBattleWithWinner)
    private val onRequestStartBattle = OnRequestStartBattle(startBattle, eventBus)
    private val onRequestFinishPlayerTurn = OnRequestFinishPlayerTurn(finishPlayerTurn, eventBus)
    private val onRequestJoinBattle = OnRequestJoinBattle(joinBattle, eventBus)
    private val onRequestLeaveBattle = OnRequestLeaveBattle(leaveBattle, eventBus)
    private val onRequestDefeatPlayer = OnRequestDefeatPlayer(defeatPlayer, eventBus)
    private val onRequestResolveDisengagedBattle = OnRequestResolveDisengagedBattle(resolveDisengagedBattle, eventBus)
}
