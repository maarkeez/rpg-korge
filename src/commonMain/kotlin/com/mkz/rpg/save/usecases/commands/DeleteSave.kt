package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveEvent.SaveDeleted
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.shared.domain.EventBus

class DeleteSave(
    private val saveRepository: SaveRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(slot: Int) {
        Save.requireSlot(slot)
        saveRepository.delete(slot)
        eventBus.publish(SaveDeleted(slot = slot))
    }
}
