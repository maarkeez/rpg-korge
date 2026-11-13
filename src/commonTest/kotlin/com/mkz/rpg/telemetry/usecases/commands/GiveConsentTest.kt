package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.adapters.storage.FacadeTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.NativeStorageTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.ProductEventSender
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.io.net.http.FakeHttpClient
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GiveConsentTest {
    @Test
    fun `GiveConsent should make an install id kept on the device`() {
        // Given
        val storage = InmemoryStorage()
        val telemetryRepository =
            NativeStorageTelemetryRepository(storage).let { nativeStorageTelemetryRepository ->
                FacadeTelemetryRepository(
                    nativeStorageTelemetryRepository,
                    ProductEventSender(nativeStorageTelemetryRepository, RemoteTelemetryRepository(collectorUrl = null, httpClient = FakeHttpClient()), TestScope(UnconfinedTestDispatcher())),
                )
            }
        // When
        GiveConsent(telemetryRepository, now = { 1_000.0 }, newInstallId = { "i1" })()
        // Then
        val dto = telemetryRepository.search().shouldNotBeNull().toDto()
        dto.consent.status shouldBe ConsentStatusDto.GIVEN
        dto.consent.decidedAt shouldBe 1_000.0
        storage.getOrNull("telemetry.installId") shouldBe "i1"
    }
}
