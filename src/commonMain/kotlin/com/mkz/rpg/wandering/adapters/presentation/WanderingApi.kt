package com.mkz.rpg.wandering.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.wandering.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.wandering.adapters.events.OnBattlefieldTileOccupied
import com.mkz.rpg.wandering.adapters.events.OnExplorationSecondStarted
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingRepository
import com.mkz.rpg.wandering.usecases.commands.ClaimHome
import com.mkz.rpg.wandering.usecases.commands.ResolveWanderingStep
import com.mkz.rpg.wandering.usecases.commands.WanderWhileExploring
import com.mkz.rpg.wandering.usecases.services.WanderingSteps

class WanderingApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    cpuBrainApi: CpuBrainApi,
    eventBus: EventBus,
) {
    // Storage
    private val wanderingRepository: WanderingRepository = InMemoryWanderingRepository()

    // Services
    private val wanderingSteps = WanderingSteps(wanderingRepository, cpuBrainApi.whereShouldWander, eventBus)

    // Commands
    val claimHome = ClaimHome(wanderingRepository, battleUnitApi.searchBattleUnitById, playerApi.searchPlayerById)
    val wanderWhileExploring = WanderWhileExploring(wanderingRepository, wanderingSteps)
    val resolveWanderingStep = ResolveWanderingStep(wanderingRepository, wanderingSteps)

    // Events
    private val onBattleUnitDeployed = OnBattleUnitDeployed(claimHome, eventBus)
    private val onExplorationSecondStarted = OnExplorationSecondStarted(wanderWhileExploring, eventBus)
    private val onBattlefieldTileOccupied = OnBattlefieldTileOccupied(resolveWanderingStep, eventBus)
}
