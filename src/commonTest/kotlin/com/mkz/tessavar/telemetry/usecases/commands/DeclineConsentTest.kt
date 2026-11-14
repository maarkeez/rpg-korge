package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.FakeTelemetryRepository
import com.mkz.tessavar.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DeclineConsentTest {
    @Test
    fun `DeclineConsent should delete the install id when consent was given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        DeclineConsent(telemetryRepository) { 1_000.0 }()
        // Then
        val dto = telemetryRepository.search().shouldNotBeNull().toDto()
        dto.consent.status shouldBe ConsentStatusDto.DECLINED
        dto.installId.shouldBeNull()
    }
}
