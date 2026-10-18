package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.domain.WanderingRepository

/** A defeated CPU battle unit starts counting the party steps until it respawns on its home. */
class AwaitRespawn(
    private val wanderingRepository: WanderingRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val wandering = wanderingRepository.search() ?: return
        wanderingRepository.save(wandering.defeat(battleUnitId))
    }
}
