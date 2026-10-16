package com.mkz.rpg.mapBuilder.adapters.storage

import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository

class InMemoryMapBuilderRepository : MapBuilderRepository {
    private var mapBuilder: MapBuilder? = null

    override fun save(mapBuilder: MapBuilder) {
        this.mapBuilder = mapBuilder
    }

    override fun search(): MapBuilder? = mapBuilder
}
