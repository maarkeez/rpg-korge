package com.mkz.rpg.cpuWanderBrain.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnBattleUnitRespawned
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnExplorationSecondStarted
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnPartyStepped
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnRoundFinished
import com.mkz.rpg.cpuWanderBrain.adapters.events.OnTileOccupied
import com.mkz.rpg.cpuWanderBrain.adapters.save.CpuWanderBrainSnapshottable
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.commands.AwaitRespawn
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ClaimHome
import com.mkz.rpg.cpuWanderBrain.usecases.commands.LoadCpuWanderBrainSnapshot
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveRespawn
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.rpg.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.rpg.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.rpg.cpuWanderBrain.usecases.queries.SearchCpuWanderBrainSnapshot
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

class CpuWanderBrainApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    cpuBattleBrainApi: CpuBattleBrainApi,
    partyApi: PartyApi,
    eventBus: EventBus,
) {
    // Storage
    private val cpuWanderBrainRepository: CpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()

    // Save
    private val searchCpuWanderBrainSnapshot = SearchCpuWanderBrainSnapshot(cpuWanderBrainRepository)
    private val loadCpuWanderBrainSnapshot = LoadCpuWanderBrainSnapshot(cpuWanderBrainRepository)
    val snapshottable: Snapshottable = CpuWanderBrainSnapshottable(searchCpuWanderBrainSnapshot, loadCpuWanderBrainSnapshot)

    // Queries
    val isInSight = IsInSight(partyApi.searchParty, battlefieldApi.searchPosition, battlefieldApi.searchBattlefieldSize)

    // Services
    private val wanderingSteps = WanderingSteps(cpuWanderBrainRepository, cpuBattleBrainApi.whereShouldWander, cpuBattleBrainApi.isOutsideTerritory, battlefieldApi.searchPosition, eventBus)

    // Commands
    val claimHome = ClaimHome(cpuWanderBrainRepository, battleUnitApi.searchBattleUnitById, playerApi.searchPlayerById, cpuBattleBrainApi.searchDistancesFromHome)
    val wanderWhileExploring = WanderWhileExploring(cpuWanderBrainRepository, wanderingSteps, isInSight, eventBus)
    val wanderAfterRound = WanderAfterRound(cpuWanderBrainRepository, wanderingSteps, isInSight, eventBus)
    val resolveWanderingStep = ResolveWanderingStep(cpuWanderBrainRepository, wanderingSteps)
    val awaitRespawn = AwaitRespawn(cpuWanderBrainRepository)
    val respawnDefeatedBattleUnits = RespawnDefeatedBattleUnits(cpuWanderBrainRepository, battlefieldApi.isTileVacant, eventBus)
    val resolveRespawn = ResolveRespawn(cpuWanderBrainRepository)

    // Events
    private val onBattleUnitDeployed = OnBattleUnitDeployed(claimHome, eventBus)
    private val onExplorationSecondStarted = OnExplorationSecondStarted(wanderWhileExploring, eventBus)
    private val onTileOccupied = OnTileOccupied(resolveWanderingStep, eventBus)
    private val onRoundFinished = OnRoundFinished(wanderAfterRound, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(awaitRespawn, eventBus)
    private val onPartyStepped = OnPartyStepped(respawnDefeatedBattleUnits, eventBus)
    private val onBattleUnitRespawned = OnBattleUnitRespawned(resolveRespawn, eventBus)
}
