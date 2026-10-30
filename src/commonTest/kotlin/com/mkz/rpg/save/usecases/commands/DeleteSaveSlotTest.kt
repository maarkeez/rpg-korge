package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class DeleteSaveSlotTest {
    private val storage = InmemoryStorage()
    private val saveRepository = NativeStorageSaveRepository(storage)
    private val eventBus = FakeEventBus()
    private val deleteSaveSlot = DeleteSaveSlot(saveRepository, eventBus)

    @Test
    fun `DeleteSaveSlot should empty the slot when a game is saved in it`() {
        // Given
        saveRepository.save(Save.save(slot = 3, savedAt = 0.0, leaderUnitId = "knight", partySize = 1, snapshots = emptyMap()))
        // When
        deleteSaveSlot(slot = 3)
        // Then
        saveRepository.searchSummary(3).shouldBeNull()
        eventBus.shouldHavePublishedEvents(SaveEvent.SaveSlotDeleted(slot = 3))
    }

    @Test
    fun `DeleteSaveSlot should empty the slot when its game can not be read`() {
        // Given
        storage["save.slot1"] = "not a save"
        storage["save.slot1.summary"] = "not a save"
        // When
        deleteSaveSlot(slot = 1)
        // Then
        storage.keys().shouldBeEmpty()
    }
}
