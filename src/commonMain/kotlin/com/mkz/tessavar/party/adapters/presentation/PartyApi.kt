package com.mkz.tessavar.party.adapters.presentation

import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.party.adapters.events.OnBattleFinished
import com.mkz.tessavar.party.adapters.events.OnBattleStarted
import com.mkz.tessavar.party.adapters.events.OnBattleUnitDefeated
import com.mkz.tessavar.party.adapters.events.OnBattleUnitMoved
import com.mkz.tessavar.party.adapters.events.OnRequestApplyMemberEffect
import com.mkz.tessavar.party.adapters.events.OnRequestJoinParty
import com.mkz.tessavar.party.adapters.events.OnRequestRecruitMember
import com.mkz.tessavar.party.adapters.events.OnRequestRemoveMemberEquippedEffects
import com.mkz.tessavar.party.adapters.events.OnRequestStartExploration
import com.mkz.tessavar.party.adapters.events.OnRequestStartTalk
import com.mkz.tessavar.party.adapters.events.OnTileOccupied
import com.mkz.tessavar.party.adapters.save.PartySnapshottable
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.party.usecases.commands.ApplyMemberEffect
import com.mkz.tessavar.party.usecases.commands.ApplyStandingTerrainEffects
import com.mkz.tessavar.party.usecases.commands.CloseFormation
import com.mkz.tessavar.party.usecases.commands.ClosePause
import com.mkz.tessavar.party.usecases.commands.CloseQuest
import com.mkz.tessavar.party.usecases.commands.FinishTalk
import com.mkz.tessavar.party.usecases.commands.InterruptExploration
import com.mkz.tessavar.party.usecases.commands.JoinParty
import com.mkz.tessavar.party.usecases.commands.LeaveParty
import com.mkz.tessavar.party.usecases.commands.LoadPartySnapshot
import com.mkz.tessavar.party.usecases.commands.MarkBattleUnitMoved
import com.mkz.tessavar.party.usecases.commands.OpenFormation
import com.mkz.tessavar.party.usecases.commands.OpenPause
import com.mkz.tessavar.party.usecases.commands.OpenQuest
import com.mkz.tessavar.party.usecases.commands.RecruitMember
import com.mkz.tessavar.party.usecases.commands.RemoveMemberEquippedEffects
import com.mkz.tessavar.party.usecases.commands.ResolvePartyStep
import com.mkz.tessavar.party.usecases.commands.ResumeExploration
import com.mkz.tessavar.party.usecases.commands.StartExploration
import com.mkz.tessavar.party.usecases.commands.StartTalk
import com.mkz.tessavar.party.usecases.commands.StepParty
import com.mkz.tessavar.party.usecases.queries.CanMemberLearnAbility
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.party.usecases.queries.SearchPartySnapshot
import com.mkz.tessavar.party.usecases.services.PartyStepping
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable

class PartyApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    private val partyRepository: PartyRepository = InMemoryPartyRepository()

    private val searchPartySnapshot = SearchPartySnapshot(partyRepository)
    private val loadPartySnapshot = LoadPartySnapshot(partyRepository, battleUnitApi.searchBattleUnitById)
    val snapshottable: Snapshottable = PartySnapshottable(searchPartySnapshot, loadPartySnapshot)

    private val partyStepping = PartyStepping(partyRepository, battlefieldApi.searchPosition, battlefieldApi.searchShortestPath, eventBus)

    val startExploration = StartExploration(playerApi.searchHumanPlayer, battleUnitApi.searchBattleUnitsByPlayerId, partyRepository, eventBus)
    val stepParty = StepParty(partyRepository, battlefieldApi.searchPosition, battlefieldApi.searchOccupant, partyStepping, eventBus)
    val resolvePartyStep = ResolvePartyStep(partyRepository, partyStepping)
    val interruptExploration = InterruptExploration(partyRepository, eventBus)
    val resumeExploration = ResumeExploration(partyRepository, eventBus)
    val leaveParty = LeaveParty(partyRepository, eventBus)
    val joinParty = JoinParty(partyRepository, battleUnitApi.searchBattleUnitById, eventBus)
    val recruitMember = RecruitMember(partyRepository, eventBus)
    val markBattleUnitMoved = MarkBattleUnitMoved(partyRepository)
    val startTalk = StartTalk(partyRepository, eventBus)
    val finishTalk = FinishTalk(partyRepository, eventBus)
    val openFormation = OpenFormation(partyRepository, eventBus)
    val closeFormation = CloseFormation(partyRepository, battlefieldApi.searchPosition, eventBus)
    val openPause = OpenPause(partyRepository, eventBus)
    val closePause = ClosePause(partyRepository, eventBus)
    val openQuest = OpenQuest(partyRepository, eventBus)
    val closeQuest = CloseQuest(partyRepository, eventBus)
    val applyStandingTerrainEffects = ApplyStandingTerrainEffects(partyRepository, battlefieldApi.searchBattlefield, battleUnitApi.searchBattleUnitById, eventBus)
    val applyMemberEffect = ApplyMemberEffect(partyRepository, eventBus)
    val removeMemberEquippedEffects = RemoveMemberEquippedEffects(partyRepository, eventBus)

    val searchParty = SearchParty(partyRepository)
    val canMemberLearnAbility = CanMemberLearnAbility(partyRepository, battleUnitApi.canLearnAbility)

    private val onRequestStartExploration = OnRequestStartExploration(startExploration, eventBus)
    private val onTileOccupied = OnTileOccupied(resolvePartyStep, eventBus)
    private val onBattleStarted = OnBattleStarted(interruptExploration, eventBus)
    private val onBattleFinished = OnBattleFinished(resumeExploration, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(leaveParty, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(markBattleUnitMoved, eventBus)
    private val onRequestRecruitMember = OnRequestRecruitMember(recruitMember, eventBus)
    private val onRequestJoinParty = OnRequestJoinParty(joinParty, eventBus)
    private val onRequestStartTalk = OnRequestStartTalk(startTalk, eventBus)
    private val onRequestApplyMemberEffect = OnRequestApplyMemberEffect(applyMemberEffect, eventBus)
    private val onRequestRemoveMemberEquippedEffects = OnRequestRemoveMemberEquippedEffects(removeMemberEquippedEffects, eventBus)
}
