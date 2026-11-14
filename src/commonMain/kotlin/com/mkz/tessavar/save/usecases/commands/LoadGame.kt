package com.mkz.tessavar.save.usecases.commands

import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveError.IncompatibleSave
import com.mkz.tessavar.save.domain.SaveError.SlotEmpty
import com.mkz.tessavar.save.domain.SaveRepository
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable

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
                snapshottable.load(requireNotNull(snapshots[snapshottable.key]) { "No snapshot of ${snapshottable.key}" })
            } catch (failure: Throwable) {
                throw IncompatibleSave(slot, failure)
            }
        }
        eventBus.publish(loadedSave.pullEvents().first)
    }
}
