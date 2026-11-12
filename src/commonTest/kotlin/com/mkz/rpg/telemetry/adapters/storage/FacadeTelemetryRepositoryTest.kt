package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.adapters.storage.ProductEventMother.productEventRecorded
import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FacadeTelemetryRepositoryTest {
    private val storage = InmemoryStorage()
    private val nativeStorageTelemetryRepository = NativeStorageTelemetryRepository(storage)
    private val remoteTelemetryRepository = RecordingRemoteTelemetryRepository().apply { answer = CompletableDeferred() }
    private val repository =
        FacadeTelemetryRepository(
            nativeStorageTelemetryRepository,
            ProductEventSender(nativeStorageTelemetryRepository, remoteTelemetryRepository, TestScope(UnconfinedTestDispatcher())),
        )
    private val consented = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 1.0)

    @Test
    fun `FacadeTelemetryRepository should queue the product events recorded on the device when a telemetry is saved`() {
        // When
        repository.save(consented.loadGameId("g1").gameOver(occurredAt = 1.0))
        // Then
        nativeStorageTelemetryRepository.searchQueued().shouldContainExactly(productEventRecorded(Name.GAME_OVER, occurredAt = 1.0))
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded(Name.GAME_OVER, occurredAt = 1.0))))
    }

    @Test
    fun `FacadeTelemetryRepository should delete the queued product events when consent is declined`() {
        // Given
        repository.save(consented.loadGameId("g1").gameOver(occurredAt = 1.0))
        // When
        repository.save(consented.declineConsent(decidedAt = 2.0))
        // Then
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
        storage.keys().shouldContainExactly("telemetry.consent")
    }

    @Test
    fun `FacadeTelemetryRepository should delete the queued product events when consent was given on another privacy notice`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded()))
        val dto = consented.toDto()
        val telemetry = Telemetry.read(dto.copy(consent = dto.consent.copy(noticeVersion = Telemetry.PRIVACY_NOTICE_VERSION - 1)))
        // When
        repository.save(telemetry.startSession("s2"))
        // Then
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
        remoteTelemetryRepository.batches.shouldBeEmpty()
    }

    @Test
    fun `FacadeTelemetryRepository should keep the telemetry on the device`() {
        // When
        repository.save(consented)
        // Then
        repository.search().shouldNotBeNull().toDto() shouldBe consented.toDto()
        remoteTelemetryRepository.batches.shouldBeEmpty()
    }
}
