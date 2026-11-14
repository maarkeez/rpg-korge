package com.mkz.tessavar.save.usecases.commands

import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveError.NotPaused
import com.mkz.tessavar.save.domain.SaveRepository
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable

class SaveGame(
    private val saveRepository: SaveRepository,
    private val snapshottables: List<Snapshottable>,
    private val canSave: () -> Boolean,
    private val leaderUnitId: () -> String?,
    private val partySize: () -> Int,
    private val nowUnixMillis: () -> Double,
    private val eventBus: EventBus,
) {
    operator fun invoke(slot: Int) {
        Save.requireSlot(slot)
        if (!canSave()) throw NotPaused()
        val snapshots = snapshottables.associate { snapshottable -> snapshottable.key to snapshottable.snapshot() }
        val (events, save) = Save.save(slot = slot, savedAt = nowUnixMillis(), leaderUnitId = leaderUnitId(), partySize = partySize(), snapshots = snapshots).pullEvents()
        saveRepository.save(save)
        eventBus.publish(events)
    }
}
