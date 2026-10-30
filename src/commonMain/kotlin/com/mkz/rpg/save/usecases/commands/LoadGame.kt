package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveError.SlotEmpty
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

/**
 * Loads the game saved in the slot: restores the snapshot of each subdomain, in order, as the game was saved, without
 * announcing any of them, then announces the game is restored (`GameRestored`), so the screen draws it all.
 *
 * Fails with [IncompatibleSave] when it was saved with another version, or a snapshot can not be restored (e.g.: it
 * names a unit the game no longer has, or a subdomain has no snapshot): the subdomains restored so far are left as they
 * are, so the scene is to be left.
 */
class LoadGame(
    private val saveRepository: SaveRepository,
    private val snapshottables: List<Snapshottable>,
    private val eventBus: EventBus,
) {
    operator fun invoke(slot: Int) {
        Save.requireSlot(slot)
        val save = saveRepository.search(slot) ?: throw SlotEmpty()
        val (snapshots, loadedSave) = save.load()
        snapshottables.forEach { snapshottable ->
            try {
                snapshottable.restore(requireNotNull(snapshots[snapshottable.key]) { "No snapshot of ${snapshottable.key}" })
            } catch (failure: Throwable) {
                throw IncompatibleSave(slot, failure)
            }
        }
        eventBus.publish(loadedSave.pullEvents().first)
    }
}
