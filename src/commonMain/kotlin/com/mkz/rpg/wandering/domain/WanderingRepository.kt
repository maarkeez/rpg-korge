package com.mkz.rpg.wandering.domain

interface WanderingRepository {
    fun save(wandering: Wandering)

    fun search(): Wandering?
}
