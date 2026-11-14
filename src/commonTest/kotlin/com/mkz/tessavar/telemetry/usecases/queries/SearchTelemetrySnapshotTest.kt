package com.mkz.tessavar.telemetry.usecases.queries

import com.mkz.tessavar.telemetry.domain.FakeTelemetryRepository
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchTelemetrySnapshotTest {
    @Test
    fun `SearchTelemetrySnapshot should find the game id of the game played`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().startGame(gameId = "g1", occurredAt = 1.0))
        // When
        val gameId = SearchTelemetrySnapshot(telemetryRepository)()
        // Then
        gameId shouldBe "g1"
    }

    @Test
    fun `SearchTelemetrySnapshot should find no game id when the telemetry was never kept`() {
        // When
        val gameId = SearchTelemetrySnapshot(FakeTelemetryRepository(null))()
        // Then
        gameId.shouldBeNull()
    }
}
