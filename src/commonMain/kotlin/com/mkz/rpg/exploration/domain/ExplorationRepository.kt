package com.mkz.rpg.exploration.domain

interface ExplorationRepository {
    fun save(exploration: Exploration)

    fun search(): Exploration?
}
