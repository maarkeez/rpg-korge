package com.mkz.rpg.save.adapters.storage

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class NativeStorageSaveRepositoryTest {
    private val storage = InmemoryStorage()
    private val repository = NativeStorageSaveRepository(storage)
    private val save =
        Save.save(slot = 2, savedAt = 1_793_385_720_000.0, leaderUnitId = "knight", partySize = 3, snapshots = mapOf("party" to """{"playerId":"human"}""")).pullEvents().second

    @Test
    fun `Search should read the game as it was saved when a game is saved in the slot`() {
        // Given
        repository.save(save)
        // When
        val found = repository.search(2)
        // Then
        found shouldBe save
        repository.searchSummary(2) shouldBe save.toDto().summary
        storage.keys().shouldContainExactlyInAnyOrder("save.slot2", "save.slot2.summary")
    }

    @Test
    fun `Search should find nothing when the slot is empty`() {
        // When
        val found = repository.search(1)
        // Then
        found.shouldBeNull()
        repository.searchSummary(1).shouldBeNull()
    }

    @Test
    fun `Search should fail as incompatible when the save holds a key it does not know`() {
        // Given
        storage["save.slot1"] = """{"summary":{"version":1,"savedAt":0.0,"leaderUnitId":null,"partySize":1},"snapshots":{},"autosave":true}"""
        // When
        val result = runCatching { repository.search(1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
    }

    @Test
    fun `Delete should empty the slot`() {
        // Given
        repository.save(save)
        // When
        repository.delete(2)
        // Then
        repository.search(2).shouldBeNull()
        storage.keys().shouldContainExactlyInAnyOrder()
    }
}
