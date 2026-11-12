package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoadTelemetrySnapshotTest {
    @Test
    fun `LoadTelemetrySnapshot should take the game id of the save`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        LoadTelemetrySnapshot(telemetryRepository)("g1")
        // Then
        telemetryRepository
            .search()
            .shouldNotBeNull()
            .toDto()
            .gameId shouldBe "g1"
    }
}
