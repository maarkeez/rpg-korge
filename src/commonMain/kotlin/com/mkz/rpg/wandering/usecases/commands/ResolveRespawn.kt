package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.domain.WanderingRepository

/** The battle unit respawned on its home, so it no longer counts the party steps. */
class ResolveRespawn(
    private val wanderingRepository: WanderingRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val wandering = wanderingRepository.search() ?: return
        wanderingRepository.save(wandering.respawn(battleUnitId))
    }
}
