package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded
import korlibs.io.lang.UTF8
import korlibs.io.lang.toByteArray
import korlibs.io.net.http.Http
import korlibs.io.net.http.HttpClient
import korlibs.io.stream.openAsync
import kotlinx.coroutines.CancellationException

open class RemoteTelemetryRepository(
    private val collectorUrl: String?,
    private val httpClient: HttpClient,
) {
    private companion object {
        const val TOO_MANY_REQUESTS = 429
        val ACCEPTED_STATUSES = 200..299
        val REJECTED_STATUSES = 400..499
    }

    enum class Delivery {
        ACCEPTED,
        REJECTED,
        NOT_RECEIVED,
    }

    open suspend fun create(productEvents: List<ProductEventRecorded>): Delivery {
        val url = collectorUrl ?: return Delivery.ACCEPTED
        val status =
            try {
                httpClient
                    .request(
                        Http.Method.POST,
                        "$url/product-events",
                        Http.Headers("Content-Type" to "application/json"),
                        ProductEventJson.encodeBatch(productEvents).toByteArray(UTF8).openAsync(),
                    ).status
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                return Delivery.NOT_RECEIVED
            }
        return when (status) {
            in ACCEPTED_STATUSES -> Delivery.ACCEPTED
            TOO_MANY_REQUESTS -> Delivery.NOT_RECEIVED
            in REJECTED_STATUSES -> Delivery.REJECTED
            else -> Delivery.NOT_RECEIVED
        }
    }
}
