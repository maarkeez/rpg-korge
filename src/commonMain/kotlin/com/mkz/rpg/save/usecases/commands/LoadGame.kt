package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveError.SlotEmpty
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

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
