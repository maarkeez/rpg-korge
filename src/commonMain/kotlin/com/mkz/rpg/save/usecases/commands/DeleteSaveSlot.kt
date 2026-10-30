package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveEvent.SaveSlotDeleted
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.shared.domain.EventBus

/** Deletes the game saved in the slot, even an incompatible one, so the slot is empty. */
class DeleteSaveSlot(
    private val saveRepository: SaveRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(slot: Int) {
        Save.requireSlot(slot)
        saveRepository.delete(slot)
        eventBus.publish(SaveSlotDeleted(slot = slot))
    }
}
