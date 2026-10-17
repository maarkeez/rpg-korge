package com.mkz.rpg.exploration.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.exploration.adapters.events.OnBattleFinished
import com.mkz.rpg.exploration.adapters.events.OnBattleStarted
import com.mkz.rpg.exploration.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.exploration.adapters.events.OnBattlefieldTileOccupied
import com.mkz.rpg.exploration.adapters.events.OnRequestStartExploration
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.exploration.usecases.commands.LeaveParty
import com.mkz.rpg.exploration.usecases.commands.PauseExploration
import com.mkz.rpg.exploration.usecases.commands.ResolvePartyStep
import com.mkz.rpg.exploration.usecases.commands.ResumeExploration
import com.mkz.rpg.exploration.usecases.commands.StartExploration
import com.mkz.rpg.exploration.usecases.commands.WalkParty
import com.mkz.rpg.exploration.usecases.queries.SearchExploration
import com.mkz.rpg.exploration.usecases.services.PartyWalking
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus

class ExplorationApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Storage
    private val explorationRepository: ExplorationRepository = InMemoryExplorationRepository()

    // Services
    private val partyWalking = PartyWalking(explorationRepository, battlefieldApi.searchPosition, battlefieldApi.searchShortestPath, eventBus)

    // Commands
    val startExploration = StartExploration(playerApi.searchHumanPlayer, battleUnitApi.searchBattleUnitsByPlayerId, explorationRepository, eventBus)
    val walkParty = WalkParty(explorationRepository, battlefieldApi.searchPosition, partyWalking)
    val resolvePartyStep = ResolvePartyStep(explorationRepository, partyWalking)
    val pauseExploration = PauseExploration(explorationRepository, eventBus)
    val resumeExploration = ResumeExploration(explorationRepository, eventBus)
    val leaveParty = LeaveParty(explorationRepository)

    // Queries
    val searchExploration = SearchExploration(explorationRepository)

    // Events
    private val onRequestStartExploration = OnRequestStartExploration(startExploration, eventBus)
    private val onBattlefieldTileOccupied = OnBattlefieldTileOccupied(resolvePartyStep, eventBus)
    private val onBattleStarted = OnBattleStarted(pauseExploration, eventBus)
    private val onBattleFinished = OnBattleFinished(resumeExploration, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(leaveParty, eventBus)
}
