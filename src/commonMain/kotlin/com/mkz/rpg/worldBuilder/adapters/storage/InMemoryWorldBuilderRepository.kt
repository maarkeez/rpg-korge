package com.mkz.rpg.worldBuilder.adapters.storage

import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

class InMemoryWorldBuilderRepository : WorldBuilderRepository {
    private var worldBuilder: WorldBuilder? = null

    override fun save(worldBuilder: WorldBuilder) {
        this.worldBuilder = worldBuilder
    }

    override fun search(): WorldBuilder? = worldBuilder
}
