package com.mkz.tessavar.save.domain

interface SaveRepository {
    fun save(save: Save)

    fun search(slot: Int): Save?

    fun delete(slot: Int)
}
