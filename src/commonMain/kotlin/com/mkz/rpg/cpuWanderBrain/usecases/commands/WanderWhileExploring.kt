package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.domain.EventBus

/**
 * Every second while the player explores, each CPU battle unit with a home in sight of the party leader ([IsInSight])
 * rests or steps around it, one after another. The ones out of sight stay put, as nobody sees them wander, and deciding
 * where each one steps takes time. The ones no longer on the battlefield (e.g.: defeated) rest. A wandering still in
 * progress is abandoned.
 *
 * Every CPU battle unit with a home recovers [WANDERING_RECOVERY_PERCENTAGE] of its maximum health and mana, whether it
 * rests, steps or is out of sight. The defeated ones do not.
 */
class WanderWhileExploring(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
    private val isInSight: IsInSight,
    private val eventBus: EventBus,
) {
    operator fun invoke(exploringPlayerId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        val battleUnitIds = cpuWanderBrain.toDto().homes.map { home -> home.battleUnitId }
        if (battleUnitIds.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = battleUnitIds, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        val battleUnitIdsInSight = battleUnitIds.filter { battleUnitId -> isInSight(battleUnitId = battleUnitId) }
        wanderingSteps.continueWandering(cpuWanderBrain.startWhileExploring(exploringPlayerId = exploringPlayerId, battleUnitIds = battleUnitIdsInSight))
    }
}
