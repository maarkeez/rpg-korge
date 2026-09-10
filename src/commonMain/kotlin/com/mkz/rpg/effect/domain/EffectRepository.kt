package com.mkz.rpg.effect.domain

interface EffectRepository {
    fun create(effect: Effect)

    fun searchById(id: String): Effect?
}
