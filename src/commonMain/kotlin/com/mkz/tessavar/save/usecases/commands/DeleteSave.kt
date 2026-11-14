package com.mkz.tessavar.save.usecases.commands

import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveEvent.SaveDeleted
import com.mkz.tessavar.save.domain.SaveRepository
import com.mkz.tessavar.shared.domain.EventBus

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
