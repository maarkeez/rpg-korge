package com.mkz.tessavar.cpuWanderBrain.adapters.presentation

import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnBattleUnitDefeated
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnBattleUnitDeployed
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnBattleUnitRespawned
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnExplorationSecondStarted
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnPartyStepped
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnRoundFinished
import com.mkz.tessavar.cpuWanderBrain.adapters.events.OnTileOccupied
import com.mkz.tessavar.cpuWanderBrain.adapters.save.CpuWanderBrainSnapshottable
import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.AwaitRespawn
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ClaimHome
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.LoadCpuWanderBrainSnapshot
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ResolveRespawn
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.tessavar.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.tessavar.cpuWanderBrain.usecases.queries.SearchCpuWanderBrainSnapshot
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable

class CpuWanderBrainApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    cpuBattleBrainApi: CpuBattleBrainApi,
    partyApi: PartyApi,
    eventBus: EventBus,
) {
    private val cpuWanderBrainRepository: CpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()

    private val searchCpuWanderBrainSnapshot = SearchCpuWanderBrainSnapshot(cpuWanderBrainRepository)
    private val loadCpuWanderBrainSnapshot = LoadCpuWanderBrainSnapshot(cpuWanderBrainRepository)
    val snapshottable: Snapshottable = CpuWanderBrainSnapshottable(searchCpuWanderBrainSnapshot, loadCpuWanderBrainSnapshot)

    val isInSight = IsInSight(partyApi.searchParty, battlefieldApi.searchPosition, battlefieldApi.searchBattlefieldSize)

    private val wanderingSteps = WanderingSteps(cpuWanderBrainRepository, cpuBattleBrainApi.whereShouldWander, cpuBattleBrainApi.isOutsideTerritory, battlefieldApi.searchPosition, eventBus)

    val claimHome = ClaimHome(cpuWanderBrainRepository, battleUnitApi.searchBattleUnitById, playerApi.searchPlayerById, cpuBattleBrainApi.searchDistancesFromHome)
    val wanderWhileExploring = WanderWhileExploring(cpuWanderBrainRepository, wanderingSteps, isInSight, eventBus)
    val wanderAfterRound = WanderAfterRound(cpuWanderBrainRepository, wanderingSteps, isInSight, eventBus)
    val resolveWanderingStep = ResolveWanderingStep(cpuWanderBrainRepository, wanderingSteps)
    val awaitRespawn = AwaitRespawn(cpuWanderBrainRepository)
    val respawnDefeatedBattleUnits = RespawnDefeatedBattleUnits(cpuWanderBrainRepository, battlefieldApi.isTileVacant, eventBus)
    val resolveRespawn = ResolveRespawn(cpuWanderBrainRepository)

    private val onBattleUnitDeployed = OnBattleUnitDeployed(claimHome, eventBus)
    private val onExplorationSecondStarted = OnExplorationSecondStarted(wanderWhileExploring, eventBus)
    private val onTileOccupied = OnTileOccupied(resolveWanderingStep, eventBus)
    private val onRoundFinished = OnRoundFinished(wanderAfterRound, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(awaitRespawn, eventBus)
    private val onPartyStepped = OnPartyStepped(respawnDefeatedBattleUnits, eventBus)
    private val onBattleUnitRespawned = OnBattleUnitRespawned(resolveRespawn, eventBus)
}
