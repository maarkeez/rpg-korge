package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once a battle round finishes, each CPU battle unit out of the battle rests or steps around its home, one after
 * another, before the engagement is updated. It is announced also when none wanders, as the next round starts on it.
 * They recover [WANDERING_RECOVERY_PERCENTAGE] of their maximum health and mana, as while exploring.
 * Battle units: those in the battle.
 */
class WanderAfterRound(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val cpuWanderBrain = (cpuWanderBrainRepository.search() ?: CpuWanderBrain.create()).startAfterRound(battleUnitIdsInBattle = battleUnitIds)
        val wanderingBattleUnitIds = cpuWanderBrain.toDto().pendingBattleUnitIds
        if (wanderingBattleUnitIds.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = wanderingBattleUnitIds, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        wanderingSteps.continueWandering(cpuWanderBrain)
    }
}
