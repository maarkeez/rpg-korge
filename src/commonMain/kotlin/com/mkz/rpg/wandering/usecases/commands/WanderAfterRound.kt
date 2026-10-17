package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingRepository
import com.mkz.rpg.wandering.usecases.services.WanderingSteps

/**
 * Once a battle round finishes, each CPU battle unit out of the battle rests or steps around its home, one after
 * another, before the engagement is updated. It is announced also when none wanders, as the next round starts on it.
 * Battle units: those in the battle.
 */
class WanderAfterRound(
    private val wanderingRepository: WanderingRepository,
    private val wanderingSteps: WanderingSteps,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val wandering = wanderingRepository.search() ?: Wandering.create()
        wanderingSteps.continueWandering(wandering.startAfterRound(battleUnitIdsInBattle = battleUnitIds))
    }
}
