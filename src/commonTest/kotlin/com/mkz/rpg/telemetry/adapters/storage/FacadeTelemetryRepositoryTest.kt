package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ProductEventDto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class FacadeTelemetryRepositoryTest {
    private val storage = InmemoryStorage()
    private val remoteTelemetryRepository = RecordingRemoteTelemetryRepository()
    private val repository = FacadeTelemetryRepository(NativeStorageTelemetryRepository(storage), remoteTelemetryRepository)

    @Test
    fun `FacadeTelemetryRepository should send product events to the remote repository`() {
        // Given
        val productEvent =
            ProductEventDto(
                name = NameDto.GAME_OVER,
                installId = "i1",
                sessionId = "s1",
                gameId = "g1",
                occurredAt = 1.0,
                questId = null,
                slot = null,
                lootableItemId = null,
                price = null,
                unitId = null,
            )
        // When
        repository.create(productEvent)
        // Then
        remoteTelemetryRepository.created.shouldContainExactly(productEvent)
        storage.keys().shouldBeEmpty()
    }

    @Test
    fun `FacadeTelemetryRepository should keep the telemetry on the device`() {
        // Given
        val telemetry = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 1.0)
        // When
        repository.save(telemetry)
        // Then
        repository.search().shouldNotBeNull().toDto() shouldBe telemetry.toDto()
        remoteTelemetryRepository.created.shouldBeEmpty()
    }

    private class RecordingRemoteTelemetryRepository : RemoteTelemetryRepository() {
        val created = mutableListOf<ProductEventDto>()

        override fun create(productEvent: ProductEventDto) {
            created += productEvent
        }
    }
}
