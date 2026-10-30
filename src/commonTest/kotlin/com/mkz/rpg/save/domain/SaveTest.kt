package com.mkz.rpg.save.domain

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class SaveTest {
    private fun saved(slot: Int = 2) = Save.save(slot = slot, savedAt = 1_793_385_720_000.0, leaderUnitId = "knight", partySize = 3, snapshots = mapOf("party" to "{}")).pullEvents().second

    @Test
    fun `Save should announce the game is saved into the slot`() {
        // When
        val save = Save.save(slot = 2, savedAt = 0.0, leaderUnitId = "knight", partySize = 3, snapshots = emptyMap())
        // Then
        save.pullEvents().first.shouldContainExactly(SaveEvent.GameSaved(slot = 2))
        save.toDto().summary shouldBe Save.Dto.SummaryDto(version = Save.SAVE_VERSION, savedAt = 0.0, leaderUnitId = "knight", partySize = 3)
    }

    @Test
    fun `Save should fail when the slot is not one of the save slots`() {
        // When
        val result = runCatching { saved(slot = Save.SLOTS + 1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.SlotOutOfRange>()
    }

    @Test
    fun `Load should hand the snapshots and announce the game is restored when it was saved with this version`() {
        // When
        val (snapshots, loaded) = saved().load()
        // Then
        snapshots shouldBe mapOf("party" to "{}")
        loaded.pullEvents().first.shouldContainExactly(SaveEvent.GameRestored(slot = 2))
    }

    @Test
    fun `Load should fail when the game was saved with another version`() {
        // Given
        val dto = saved().toDto()
        val olderSave = Save.read(dto.copy(summary = dto.summary.copy(version = Save.SAVE_VERSION - 1)))
        // When
        val result = runCatching { olderSave.load() }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
    }

    @Test
    fun `SlotOf should show the slot as incompatible when its summary is of another version`() {
        // Given
        val summary = saved().toDto().summary
        // When
        val slots = listOf(Save.slotOf(1, null), Save.slotOf(2, summary), Save.slotOf(3, summary.copy(version = 0)))
        // Then
        slots.map(Save.Dto.SlotDto::state).shouldContainExactly(Save.Dto.SlotStateDto.EMPTY, Save.Dto.SlotStateDto.SAVED, Save.Dto.SlotStateDto.INCOMPATIBLE)
    }
}
