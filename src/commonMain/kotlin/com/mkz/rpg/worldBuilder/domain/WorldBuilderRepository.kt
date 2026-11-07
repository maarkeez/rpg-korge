package com.mkz.rpg.worldBuilder.domain

interface WorldBuilderRepository {
    /** Stores the world, replacing the previous one: there is only one world being built. */
    fun save(worldBuilder: WorldBuilder)

    fun search(): WorldBuilder?
}
