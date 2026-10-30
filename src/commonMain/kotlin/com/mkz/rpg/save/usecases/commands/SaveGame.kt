package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.NotPaused
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

/**
 * Saves the game into the slot, replacing the game saved there: the snapshot of each subdomain, in order, and the
 * summary the slot shows. Fails when the game is not paused while exploring, e.g.: in a battle.
 *
 * Can save: whether the game is paused while exploring. Leader unit: the unit of the party leader. Party size: its
 * members. Now: the time, in milliseconds since the epoch.
 */
class SaveGame(
    private val saveRepository: SaveRepository,
    private val snapshottables: List<Snapshottable>,
    private val canSave: () -> Boolean,
    private val leaderUnitId: () -> String?,
    private val partySize: () -> Int,
    private val now: () -> Double,
    private val eventBus: EventBus,
) {
    operator fun invoke(slot: Int) {
        Save.requireSlot(slot)
        if (!canSave()) throw NotPaused()
        val snapshots = snapshottables.associate { snapshottable -> snapshottable.key to snapshottable.snapshot() }
        val (events, save) = Save.save(slot = slot, savedAt = now(), leaderUnitId = leaderUnitId(), partySize = partySize(), snapshots = snapshots).pullEvents()
        saveRepository.save(save)
        eventBus.publish(events)
    }
}
