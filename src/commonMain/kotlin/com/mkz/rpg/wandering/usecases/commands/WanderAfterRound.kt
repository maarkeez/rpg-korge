package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingRepository
import com.mkz.rpg.wandering.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.wandering.usecases.services.WanderingSteps

/**
 * Once a battle round finishes, each CPU battle unit out of the battle rests or steps around its home, one after
 * another, before the engagement is updated. It is announced also when none wanders, as the next round starts on it.
 * They recover [WANDERING_RECOVERY_PERCENTAGE] of their maximum health and mana, as while exploring.
 * Battle units: those in the battle.
 */
class WanderAfterRound(
    private val wanderingRepository: WanderingRepository,
    private val wanderingSteps: WanderingSteps,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val wandering = (wanderingRepository.search() ?: Wandering.create()).startAfterRound(battleUnitIdsInBattle = battleUnitIds)
        val wanderingBattleUnitIds = wandering.toDto().pendingBattleUnitIds
        if (wanderingBattleUnitIds.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = wanderingBattleUnitIds, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        wanderingSteps.continueWandering(wandering)
    }
}
