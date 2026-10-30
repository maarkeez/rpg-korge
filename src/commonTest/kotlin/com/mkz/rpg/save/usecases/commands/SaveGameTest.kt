package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.rpg.save.domain.FakeSnapshottable
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError
import com.mkz.rpg.save.domain.SaveEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class SaveGameTest {
    private val saveRepository = NativeStorageSaveRepository(InmemoryStorage())
    private val eventBus = FakeEventBus()
    private val snapshottables = listOf(FakeSnapshottable("battleUnit"), FakeSnapshottable("battlefield"), FakeSnapshottable("party"))
    private var isPaused = true

    private val saveGame = SaveGame(saveRepository, snapshottables, { isPaused }, { "knight" }, { 3 }, { 1_793_385_720_000.0 }, eventBus)

    @Test
    fun `SaveGame should write every snapshot in order when the party is exploring`() {
        // When
        saveGame(slot = 2)
        // Then
        val save = saveRepository.search(2)!!.toDto()
        save.snapshots.keys
            .toList()
            .shouldContainExactly("battleUnit", "battlefield", "party")
        save.snapshots["party"] shouldBe "{\"party\":1}"
        save.summary shouldBe Save.Dto.SummaryDto(version = Save.SAVE_VERSION, savedAt = 1_793_385_720_000.0, leaderUnitId = "knight", partySize = 3)
        eventBus.shouldHavePublishedEvents(SaveEvent.GameSaved(slot = 2))
    }

    @Test
    fun `SaveGame should replace the game saved in the slot`() {
        // Given
        saveGame(slot = 1)
        snapshottables.last().state = "{\"party\":2}"
        // When
        saveGame(slot = 1)
        // Then
        saveRepository.search(1)!!.toDto().snapshots["party"] shouldBe "{\"party\":2}"
    }

    @Test
    fun `SaveGame should fail when the party is not exploring`() {
        // Given
        isPaused = false
        // When
        val result = runCatching { saveGame(slot = 1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.NotPaused>()
        saveRepository.search(1).shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
