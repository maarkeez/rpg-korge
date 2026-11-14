package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.FakeTelemetryRepository
import com.mkz.tessavar.telemetry.domain.Telemetry
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartSessionTest {
    @Test
    fun `StartSession should start the session of this launch when the game is launched for the first time`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(null)
        // When
        StartSession(telemetryRepository)("s2")
        // Then
        telemetryRepository.search().shouldNotBeNull().toDto() shouldBe Telemetry.notAsked().startSession("s2").toDto()
    }

    @Test
    fun `StartSession should keep the consent given in an earlier launch`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        StartSession(telemetryRepository)("s2")
        // Then
        telemetryRepository.search().shouldNotBeNull().toDto() shouldBe FakeTelemetryRepository.consented().startSession("s2").toDto()
    }
}
