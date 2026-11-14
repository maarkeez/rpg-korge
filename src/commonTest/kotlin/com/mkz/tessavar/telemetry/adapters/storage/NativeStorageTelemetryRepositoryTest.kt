package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.adapters.storage.ProductEventMother.productEventRecorded
import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class NativeStorageTelemetryRepositoryTest {
    private val storage = InmemoryStorage()
    private val repository = NativeStorageTelemetryRepository(storage)
    private val consented = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 1_000.0)

    @Test
    fun `NativeStorageTelemetryRepository should keep the consent and the install id on the device`() {
        // Given
        repository.save(consented)
        // When
        val found = NativeStorageTelemetryRepository(storage).search()
        // Then
        found.shouldNotBeNull().toDto() shouldBe consented.toDto().copy(sessionId = null)
        storage.keys().shouldContainExactlyInAnyOrder("telemetry.consent", "telemetry.installId")
    }

    @Test
    fun `NativeStorageTelemetryRepository should keep the session id and the game id in memory only`() {
        // Given
        repository.save(consented.startGame(gameId = "g1", occurredAt = 1.0))
        // When
        val found = repository.search()
        // Then
        found.shouldNotBeNull().toDto().sessionId shouldBe "s1"
        found.toDto().gameId shouldBe "g1"
        storage.toMap().values.none { value -> value?.contains("s1") == true || value?.contains("g1") == true } shouldBe true
    }

    @Test
    fun `NativeStorageTelemetryRepository should delete the install id from the device when it is forgotten`() {
        // Given
        repository.save(consented)
        // When
        repository.save(consented.declineConsent(decidedAt = 2_000.0))
        // Then
        storage.keys().shouldContainExactlyInAnyOrder("telemetry.consent")
    }

    @Test
    fun `NativeStorageTelemetryRepository should write nothing on the device when consent was never asked`() {
        // When
        repository.save(Telemetry.notAsked().startSession("s1"))
        // Then
        storage.keys().shouldBeEmpty()
    }

    @Test
    fun `NativeStorageTelemetryRepository should find nothing when nothing was kept`() {
        // When
        val found = repository.search()
        // Then
        found.shouldBeNull()
    }

    @Test
    fun `NativeStorageTelemetryRepository should find the consent not asked when the device holds a consent it can not read`() {
        // Given
        repository.save(Telemetry.notAsked().startSession("s1"))
        storage["telemetry.consent"] = """{"status":"MAYBE","noticeVersion":1,"decidedAt":0.0}"""
        // When
        val found = repository.search()
        // Then
        found.shouldNotBeNull().toDto().consent shouldBe Telemetry.notAsked().toDto().consent
    }

    @Test
    fun `NativeStorageTelemetryRepository should keep the queued product events on the device`() {
        // Given
        repository.queue(listOf(productEventRecorded(Name.QUEST_STARTED)))
        repository.queue(listOf(productEventRecorded(Name.QUEST_COMPLETED)))
        // When
        val queued = NativeStorageTelemetryRepository(storage).searchQueued()
        // Then
        queued.shouldContainExactly(productEventRecorded(Name.QUEST_STARTED), productEventRecorded(Name.QUEST_COMPLETED))
        storage.keys().shouldContainExactly("telemetry.productEvents")
    }

    @Test
    fun `NativeStorageTelemetryRepository should remove only the product events it is given when some are sent`() {
        // Given
        repository.queue(listOf(productEventRecorded(Name.ITEM_BOUGHT)))
        val batch = repository.searchQueued()
        repository.deleteQueued()
        repository.queue(listOf(productEventRecorded(Name.ITEM_SOLD)))
        // When
        repository.removeQueued(batch)
        // Then
        repository.searchQueued().shouldContainExactly(productEventRecorded(Name.ITEM_SOLD))
    }

    @Test
    fun `NativeStorageTelemetryRepository should drop the oldest product event when 1,000 are queued`() {
        // Given
        repository.queue(List(NativeStorageTelemetryRepository.MAX_QUEUED_PRODUCT_EVENTS) { index -> productEventRecorded(occurredAt = index.toDouble()) })
        // When
        repository.queue(listOf(productEventRecorded(Name.BATTLE_STARTED, occurredAt = 1_000.0)))
        // Then
        val queued = repository.searchQueued()
        queued.size shouldBe NativeStorageTelemetryRepository.MAX_QUEUED_PRODUCT_EVENTS
        queued.first() shouldBe productEventRecorded(occurredAt = 1.0)
        queued.last() shouldBe productEventRecorded(Name.BATTLE_STARTED, occurredAt = 1_000.0)
    }
}
