package com.mkz.tessavar.worldBuilder.domain

interface WorldBuilderRepository {
    fun save(worldBuilder: WorldBuilder)

    fun search(): WorldBuilder?
}
