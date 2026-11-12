package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
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
    fun `NativeStorageTelemetryRepository should keep the session id in memory only`() {
        // Given
        repository.save(Telemetry.notAsked().startSession("s1"))
        // When
        val found = repository.search()
        // Then
        found.shouldNotBeNull().toDto().sessionId shouldBe "s1"
        storage.toMap().values.none { value -> value?.contains("s1") == true } shouldBe true
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
}
