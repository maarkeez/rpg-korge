package com.mkz.tessavar.telemetry.adapters.save

import com.mkz.tessavar.telemetry.domain.FakeTelemetryRepository
import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.usecases.commands.LoadTelemetrySnapshot
import com.mkz.tessavar.telemetry.usecases.queries.SearchTelemetrySnapshot
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TelemetrySnapshottableTest {
    private fun snapshottable(telemetryRepository: FakeTelemetryRepository) = TelemetrySnapshottable(SearchTelemetrySnapshot(telemetryRepository), LoadTelemetrySnapshot(telemetryRepository))

    @Test
    fun `TelemetrySnapshottable should save the game id when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().startGame(gameId = "g1", occurredAt = 1.0))
        // When
        val snapshot = snapshottable(telemetryRepository).snapshot()
        // Then
        snapshot shouldBe """{"gameId":"g1"}"""
    }

    @Test
    fun `TelemetrySnapshottable should save no game id when consent is declined`() {
        // Given
        val declined =
            Telemetry
                .notAsked()
                .startSession("s1")
                .declineConsent(decidedAt = 0.0)
                .startGame(gameId = "g1", occurredAt = 1.0)
        // When
        val snapshot = snapshottable(FakeTelemetryRepository(declined)).snapshot()
        // Then
        snapshot shouldBe """{"gameId":null}"""
    }

    @Test
    fun `TelemetrySnapshottable should load the game id of the save`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        snapshottable(telemetryRepository).load("""{"gameId":"g1"}""")
        // Then
        telemetryRepository
            .search()
            .shouldNotBeNull()
            .toDto()
            .gameId shouldBe "g1"
    }
}
