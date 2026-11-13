package com.mkz.rpg.save.domain

interface SaveRepository {
    fun save(save: Save)

    fun search(slot: Int): Save?

    fun delete(slot: Int)
}
