package com.mkz.rpg.exploration.adapters.storage

import com.mkz.rpg.exploration.domain.Exploration
import com.mkz.rpg.exploration.domain.ExplorationRepository

// Holds the exploration of the game in progress.
class InMemoryExplorationRepository : ExplorationRepository {
    private var exploration: Exploration? = null

    override fun save(exploration: Exploration) {
        this.exploration = exploration
    }

    override fun search() = exploration
}
