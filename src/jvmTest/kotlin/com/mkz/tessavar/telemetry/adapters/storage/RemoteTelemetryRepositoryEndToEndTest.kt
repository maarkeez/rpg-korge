package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository.Delivery
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.io.net.http.createHttpClient
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test

class RemoteTelemetryRepositoryEndToEndTest {
    private val collectorUrl =
        checkNotNull(System.getProperty("rpg.telemetryCollectorUrl")?.takeIf(String::isNotBlank)) {
            "Run it with ./gradlew jvmTelemetryCollectorTest -Prpg.telemetryCollectorUrl=http://localhost:8787, against wrangler dev: see telemetry-collector/README.md"
        }
    private val remoteTelemetryRepository = RemoteTelemetryRepository(collectorUrl, createHttpClient())
    private val sampleBatch = ProductEventJson.decodeBatch(File("src/jvmTest/fixtures/telemetry/product-events.json").readText())

    @Test
    fun `RemoteTelemetryRepository should have the sample batch accepted by the telemetry collector`() =
        runTest {
            // When
            val delivery = remoteTelemetryRepository.create(sampleBatch)
            // Then
            delivery shouldBe Delivery.ACCEPTED
        }

    @Test
    fun `RemoteTelemetryRepository should have a batch of 101 product events rejected by the telemetry collector`() =
        runTest {
            // Given
            val productEvents = List(101) { index -> sampleBatch.first().copy(occurredAt = index.toDouble()) }
            // When
            val delivery = remoteTelemetryRepository.create(productEvents)
            // Then
            delivery shouldBe Delivery.REJECTED
        }

    @Test
    fun `RemoteTelemetryRepository should have a batch sent again accepted by the telemetry collector`() =
        runTest {
            // When
            val deliveries = List(2) { remoteTelemetryRepository.create(sampleBatch) }
            // Then
            deliveries.shouldContainExactly(Delivery.ACCEPTED, Delivery.ACCEPTED)
        }
}
