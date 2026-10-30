package com.mkz.rpg.save.usecases.commands

import com.mkz.rpg.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.rpg.save.domain.FakeSnapshottable
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError
import com.mkz.rpg.save.domain.SaveEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class LoadGameTest {
    private val storage = InmemoryStorage()
    private val saveRepository = NativeStorageSaveRepository(storage)
    private val eventBus = FakeEventBus()
    private val restored = mutableListOf<String>()

    private fun snapshottables(failure: Throwable? = null) =
        listOf(
            FakeSnapshottable("battleUnit", restored = restored, failure = failure),
            FakeSnapshottable("battlefield", restored = restored),
            FakeSnapshottable("party", restored = restored),
        )

    private fun save(snapshots: Map<String, String> = mapOf("battleUnit" to "{\"hp\":18}", "battlefield" to "{}", "party" to "{}")) =
        saveRepository.save(Save.save(slot = 2, savedAt = 0.0, leaderUnitId = "knight", partySize = 3, snapshots = snapshots))

    @Test
    fun `LoadGame should restore every snapshot in order and publish GameRestored when the slot is compatible`() {
        // Given
        save()
        val snapshottables = snapshottables()
        // When
        LoadGame(saveRepository, snapshottables, eventBus)(slot = 2)
        // Then
        restored.shouldContainExactly("battleUnit", "battlefield", "party")
        snapshottables.first().snapshot() shouldBe "{\"hp\":18}"
        eventBus.shouldHavePublishedEvents(SaveEvent.GameRestored(slot = 2))
    }

    @Test
    fun `LoadGame should refuse when the save is of another version`() {
        // Given
        storage["save.slot2"] = """{"summary":{"version":0,"savedAt":0.0,"leaderUnitId":"knight","partySize":1},"snapshots":{}}"""
        // When
        val result = runCatching { LoadGame(saveRepository, snapshottables(), eventBus)(slot = 2) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
        restored.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `LoadGame should refuse when a snapshot names an id the resources no longer have`() {
        // Given
        save()
        // When
        val result = runCatching { LoadGame(saveRepository, snapshottables(failure = IllegalArgumentException("Unknown unit goblin-archer")), eventBus)(slot = 2) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
        result.exceptionOrNull()?.message.orEmpty() shouldContain "goblin-archer"
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `LoadGame should refuse when a subdomain has no snapshot`() {
        // Given
        save(snapshots = mapOf("battleUnit" to "{}"))
        // When
        val result = runCatching { LoadGame(saveRepository, snapshottables(), eventBus)(slot = 2) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
    }

    @Test
    fun `LoadGame should fail when the slot is empty`() {
        // When
        val result = runCatching { LoadGame(saveRepository, snapshottables(), eventBus)(slot = 1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.SlotEmpty>()
    }
}
