package com.mkz.rpg.save.domain

interface SaveRepository {
    fun save(save: Save)

    fun search(slot: Int): Save?

    fun searchSummary(slot: Int): Save.Dto.SummaryDto?

    fun delete(slot: Int)
}
