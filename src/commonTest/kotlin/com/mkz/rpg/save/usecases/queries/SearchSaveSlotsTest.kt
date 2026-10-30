package com.mkz.rpg.save.usecases.queries

import com.mkz.rpg.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.Save.Dto.SlotStateDto
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class SearchSaveSlotsTest {
    private val storage = InmemoryStorage()
    private val saveRepository = NativeStorageSaveRepository(storage)
    private val searchSaveSlots = SearchSaveSlots(saveRepository)

    @Test
    fun `SearchSaveSlots should list the 3 slots, empty, saved or incompatible, when some are saved`() {
        // Given: slot 2 saved by this game, slot 3 by an older one.
        saveRepository.save(Save.save(slot = 2, savedAt = 0.0, leaderUnitId = "knight", partySize = 3, snapshots = emptyMap()))
        storage["save.slot3.summary"] = """{"version":0,"savedAt":0.0,"leaderUnitId":"knight","partySize":1}"""
        // When
        val slots = searchSaveSlots()
        // Then
        slots.map(Save.Dto.SlotDto::slot).shouldContainExactly(1, 2, 3)
        slots.map(Save.Dto.SlotDto::state).shouldContainExactly(SlotStateDto.EMPTY, SlotStateDto.SAVED, SlotStateDto.INCOMPATIBLE)
        slots[1].summary?.leaderUnitId shouldBe "knight"
    }

    @Test
    fun `SearchSaveSlots should show a slot as incompatible when its summary can not be read`() {
        // Given
        storage["save.slot1.summary"] = "not a save"
        // When
        val slots = searchSaveSlots()
        // Then
        slots.first().state shouldBe SlotStateDto.INCOMPATIBLE
    }
}
