package com.mkz.rpg.party.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.events.OnBattleFinished
import com.mkz.rpg.party.adapters.events.OnBattleStarted
import com.mkz.rpg.party.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.party.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.party.adapters.events.OnBattlefieldTileOccupied
import com.mkz.rpg.party.adapters.events.OnRequestJoinParty
import com.mkz.rpg.party.adapters.events.OnRequestStartExploration
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.party.usecases.commands.ApplyStandingTerrainEffects
import com.mkz.rpg.party.usecases.commands.FinishTalk
import com.mkz.rpg.party.usecases.commands.JoinParty
import com.mkz.rpg.party.usecases.commands.LeaveParty
import com.mkz.rpg.party.usecases.commands.MarkBattleUnitMoved
import com.mkz.rpg.party.usecases.commands.PauseExploration
import com.mkz.rpg.party.usecases.commands.ResolvePartyStep
import com.mkz.rpg.party.usecases.commands.ResumeExploration
import com.mkz.rpg.party.usecases.commands.StartExploration
import com.mkz.rpg.party.usecases.commands.TalkToAdjacentNpc
import com.mkz.rpg.party.usecases.commands.WalkParty
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus

class PartyApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    npcApi: NpcApi,
    eventBus: EventBus,
) {
    // Storage
    private val partyRepository: PartyRepository = InMemoryPartyRepository()

    // Services
    private val partyWalking = PartyWalking(partyRepository, battlefieldApi.searchPosition, battlefieldApi.searchShortestPath, eventBus)

    // Commands
    val startExploration = StartExploration(playerApi.searchHumanPlayer, battleUnitApi.searchBattleUnitsByPlayerId, partyRepository, eventBus)
    val walkParty = WalkParty(partyRepository, battlefieldApi.searchPosition, partyWalking)
    val resolvePartyStep = ResolvePartyStep(partyRepository, partyWalking)
    val pauseExploration = PauseExploration(partyRepository, eventBus)
    val resumeExploration = ResumeExploration(partyRepository, eventBus)
    val leaveParty = LeaveParty(partyRepository)
    val joinParty = JoinParty(partyRepository, eventBus)
    val markBattleUnitMoved = MarkBattleUnitMoved(partyRepository)
    val talkToAdjacentNpc = TalkToAdjacentNpc(partyRepository, battlefieldApi.searchPosition, npcApi.searchNpcAt, eventBus)
    val finishTalk = FinishTalk(partyRepository, eventBus)
    val applyStandingTerrainEffects = ApplyStandingTerrainEffects(partyRepository, battlefieldApi.searchBattlefield, battleUnitApi.searchBattleUnitById, eventBus)

    // Queries
    val searchParty = SearchParty(partyRepository)

    // Events
    private val onRequestStartExploration = OnRequestStartExploration(startExploration, eventBus)
    private val onBattlefieldTileOccupied = OnBattlefieldTileOccupied(resolvePartyStep, eventBus)
    private val onBattleStarted = OnBattleStarted(pauseExploration, eventBus)
    private val onBattleFinished = OnBattleFinished(resumeExploration, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(leaveParty, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(markBattleUnitMoved, eventBus)
    private val onRequestJoinParty = OnRequestJoinParty(joinParty, eventBus)
}
