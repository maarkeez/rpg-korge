package com.mkz.rpg.wandering.adapters.storage

import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingRepository

// Holds the homes of the CPU battle units of the game in progress, and their wandering.
class InMemoryWanderingRepository : WanderingRepository {
    private var wandering: Wandering? = null

    override fun save(wandering: Wandering) {
        this.wandering = wandering
    }

    override fun search() = wandering
}
