package com.mkz.rpg.save.domain

interface SaveRepository {
    fun save(save: Save)

    /** The game saved in the slot, or `null` when it is empty. Fails with [SaveError.IncompatibleSave] when it can not be read. */
    fun search(slot: Int): Save?

    /**
     * What the slot shows, without reading its snapshots, or `null` when it is empty. Fails with
     * [SaveError.IncompatibleSave] when it can not be read.
     */
    fun searchSummary(slot: Int): Save.Dto.SummaryDto?

    fun delete(slot: Int)
}
