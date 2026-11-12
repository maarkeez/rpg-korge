package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.adapters.storage.FacadeTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.NativeStorageTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test

class GiveConsentTest {
    @Test
    fun `GiveConsent should make an install id kept on the device`() {
        // Given
        val storage = InmemoryStorage()
        val telemetryRepository = FacadeTelemetryRepository(NativeStorageTelemetryRepository(storage), RemoteTelemetryRepository())
        // When
        GiveConsent(telemetryRepository, now = { 1_000.0 }, newInstallId = { "i1" })()
        // Then
        val dto = telemetryRepository.search().shouldNotBeNull().toDto()
        dto.consent.status shouldBe ConsentStatusDto.GIVEN
        dto.consent.decidedAt shouldBe 1_000.0
        storage.getOrNull("telemetry.installId") shouldBe "i1"
    }
}
