package com.mkz.tessavar.worldBuilder.adapters.storage

import com.mkz.tessavar.worldBuilder.domain.WorldBuilder
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository

class InMemoryWorldBuilderRepository : WorldBuilderRepository {
    private var worldBuilder: WorldBuilder? = null

    override fun save(worldBuilder: WorldBuilder) {
        this.worldBuilder = worldBuilder
    }

    override fun search(): WorldBuilder? = worldBuilder
}
