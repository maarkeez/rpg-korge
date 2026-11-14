package com.mkz.tessavar.save.usecases.commands

import com.mkz.tessavar.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveEvent
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class DeleteSaveTest {
    private val storage = InmemoryStorage()
    private val saveRepository = NativeStorageSaveRepository(storage)
    private val eventBus = FakeEventBus()
    private val deleteSave = DeleteSave(saveRepository, eventBus)

    @Test
    fun `DeleteSave should empty the slot when a game is saved in it`() {
        // Given
        saveRepository.save(Save.save(slot = 3, savedAt = 0.0, leaderUnitId = "knight", partySize = 1, snapshots = emptyMap()))
        // When
        deleteSave(slot = 3)
        // Then
        saveRepository.search(3).shouldBeNull()
        eventBus.shouldHavePublishedEvents(SaveEvent.SaveDeleted(slot = 3))
    }

    @Test
    fun `DeleteSave should empty the slot when its game can not be read`() {
        // Given
        storage["save.slot1"] = "not a save"
        storage["save.slot1.summary"] = "not a save"
        // When
        deleteSave(slot = 1)
        // Then
        storage.keys().shouldBeEmpty()
    }
}
