package com.mkz.rpg.terrain.usecases.services

interface TerrainTransitionsService {
    fun searchTransitions(): Map<String, Set<String>>
}
