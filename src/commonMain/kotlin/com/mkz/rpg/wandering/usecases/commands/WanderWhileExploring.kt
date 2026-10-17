package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.domain.WanderingRepository
import com.mkz.rpg.wandering.usecases.services.WanderingSteps

/**
 * Every second while the player explores, each CPU battle unit with a home rests or steps around it, one after another.
 * The ones no longer on the battlefield (e.g.: defeated) rest. A wandering still in progress is abandoned.
 */
class WanderWhileExploring(
    private val wanderingRepository: WanderingRepository,
    private val wanderingSteps: WanderingSteps,
) {
    operator fun invoke(exploringPlayerId: String) {
        val wandering = wanderingRepository.search() ?: return
        val battleUnitIds = wandering.toDto().homes.map { home -> home.battleUnitId }
        wanderingSteps.continueWandering(wandering.startWhileExploring(exploringPlayerId = exploringPlayerId, battleUnitIds = battleUnitIds))
    }
}
