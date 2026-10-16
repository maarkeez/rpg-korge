package com.mkz.rpg.mapBuilder.domain

interface MapBuilderRepository {
    /** Stores the map, replacing the previous one: there is only one map being designed. */
    fun save(mapBuilder: MapBuilder)

    fun search(): MapBuilder?
}
