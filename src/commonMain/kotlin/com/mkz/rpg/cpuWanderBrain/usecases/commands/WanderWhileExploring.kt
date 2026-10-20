package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.domain.EventBus

/**
 * Every second while the player explores, each CPU battle unit with a home rests or steps around it, one after another.
 * The ones no longer on the battlefield (e.g.: defeated) rest. A wandering still in progress is abandoned.
 *
 * They recover [WANDERING_RECOVERY_PERCENTAGE] of their maximum health and mana, whether they rest or step. The
 * defeated ones do not.
 */
class WanderWhileExploring(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
    private val eventBus: EventBus,
) {
    operator fun invoke(exploringPlayerId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        val battleUnitIds = cpuWanderBrain.toDto().homes.map { home -> home.battleUnitId }
        if (battleUnitIds.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = battleUnitIds, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        wanderingSteps.continueWandering(cpuWanderBrain.startWhileExploring(exploringPlayerId = exploringPlayerId, battleUnitIds = battleUnitIds))
    }
}
