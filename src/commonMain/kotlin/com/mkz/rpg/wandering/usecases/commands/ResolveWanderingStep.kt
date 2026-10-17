package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.domain.WanderingRepository
import com.mkz.rpg.wandering.usecases.services.WanderingSteps

/** Lets the next CPU battle unit wander once the step it awaits has been walked. Other occupied tiles are ignored. */
class ResolveWanderingStep(
    private val wanderingRepository: WanderingRepository,
    private val wanderingSteps: WanderingSteps,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val wandering = wanderingRepository.search() ?: return
        val resolvedWandering = wandering.resolveStep(battleUnitId = battleUnitId, row = row, column = column)
        if (resolvedWandering == wandering) return
        wanderingSteps.continueWandering(resolvedWandering)
    }
}
