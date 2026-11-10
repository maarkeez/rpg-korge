package com.mkz.rpg.worldBuilder.domain

interface WorldBuilderRepository {
    fun save(worldBuilder: WorldBuilder)

    fun search(): WorldBuilder?
}
