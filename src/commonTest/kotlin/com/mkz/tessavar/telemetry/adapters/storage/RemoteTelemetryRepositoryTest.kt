package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.adapters.storage.ProductEventMother.productEventRecorded
import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository.Delivery
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import korlibs.io.net.http.FakeHttpClient
import korlibs.io.net.http.Http
import korlibs.io.net.http.HttpClient
import korlibs.io.stream.AsyncInputStreamWithLength
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RemoteTelemetryRepositoryTest {
    private companion object {
        const val COLLECTOR_URL = "https://telemetry-collector.example.workers.dev"
    }

    private val productEvents = listOf(productEventRecorded(Name.QUEST_STARTED), productEventRecorded(Name.QUEST_COMPLETED))

    private fun answering(status: Int) = FakeHttpClient { onRequest(Http.Method.POST, "$COLLECTOR_URL/product-events").response("", status) }

    @Test
    fun `RemoteTelemetryRepository should accept without sending when there is no telemetry collector`() =
        runTest {
            // Given
            val httpClient = answering(500)
            // When
            val delivery = RemoteTelemetryRepository(collectorUrl = null, httpClient = httpClient).create(productEvents)
            // Then
            delivery shouldBe Delivery.ACCEPTED
            httpClient.log.shouldBeEmpty()
        }

    @Test
    fun `RemoteTelemetryRepository should be accepted when the telemetry collector answers 204`() =
        runTest {
            // Given
            val httpClient = answering(204)
            // When
            val delivery = RemoteTelemetryRepository(COLLECTOR_URL, httpClient).create(productEvents)
            // Then
            delivery shouldBe Delivery.ACCEPTED
            val request = httpClient.log.single()
            request shouldStartWith "POST, $COLLECTOR_URL/product-events, "
            request shouldContain "application/json"
            request shouldEndWith ", ${ProductEventJson.encodeBatch(productEvents)}"
        }

    @Test
    fun `RemoteTelemetryRepository should be rejected when the telemetry collector answers 400`() =
        runTest {
            // When
            val delivery = RemoteTelemetryRepository(COLLECTOR_URL, answering(400)).create(productEvents)
            // Then
            delivery shouldBe Delivery.REJECTED
        }

    @Test
    fun `RemoteTelemetryRepository should not be received when the telemetry collector answers 429`() =
        runTest {
            // When
            val delivery = RemoteTelemetryRepository(COLLECTOR_URL, answering(429)).create(productEvents)
            // Then
            delivery shouldBe Delivery.NOT_RECEIVED
        }

    @Test
    fun `RemoteTelemetryRepository should not be received when the telemetry collector answers 500`() =
        runTest {
            // When
            val delivery = RemoteTelemetryRepository(COLLECTOR_URL, answering(500)).create(productEvents)
            // Then
            delivery shouldBe Delivery.NOT_RECEIVED
        }

    @Test
    fun `RemoteTelemetryRepository should not be received when the request fails`() =
        runTest {
            // Given
            val httpClient =
                object : HttpClient {
                    override suspend fun requestInternal(
                        method: Http.Method,
                        url: String,
                        headers: Http.Headers,
                        content: AsyncInputStreamWithLength?,
                    ): HttpClient.Response = throw IllegalStateException("The telemetry collector is out of reach")
                }
            // When
            val delivery = RemoteTelemetryRepository(COLLECTOR_URL, httpClient).create(productEvents)
            // Then
            delivery shouldBe Delivery.NOT_RECEIVED
        }
}
