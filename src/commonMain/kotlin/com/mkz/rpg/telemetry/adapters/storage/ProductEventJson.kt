package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The JSON of product events: the queue kept on the device, and the batch sent to the telemetry collector, whose shape
 * `src/jvmTest/fixtures/telemetry/product-events.json` holds for the tests of the telemetry collector.
 */
object ProductEventJson {
    private val json = Json { ignoreUnknownKeys = false }

    fun encodeQueue(productEvents: List<ProductEventRecorded>): String = json.encodeToString(productEvents.map(ProductEventFile::of))

    fun decodeQueue(file: String): List<ProductEventRecorded>? =
        try {
            json.decodeFromString<List<ProductEventFile>>(file).mapNotNull(ProductEventFile::toProductEventRecorded)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    fun encodeBatch(productEvents: List<ProductEventRecorded>): String = json.encodeToString(ProductEventBatchFile(productEvents.map(ProductEventFile::of)))

    fun decodeBatch(file: String): List<ProductEventRecorded> = json.decodeFromString<ProductEventBatchFile>(file).productEvents.mapNotNull(ProductEventFile::toProductEventRecorded)
}

@Serializable
private data class ProductEventBatchFile(
    val productEvents: List<ProductEventFile>,
)

@Serializable
private data class ProductEventFile(
    val name: String,
    val installId: String,
    val sessionId: String,
    val gameId: String?,
    val occurredAt: Double,
    val questId: String?,
    val slot: Int?,
    val lootableItemId: String?,
    val price: Int?,
    val unitId: String?,
) {
    companion object {
        fun of(productEvent: ProductEventRecorded) =
            ProductEventFile(
                name = productEvent.name.name,
                installId = productEvent.installId,
                sessionId = productEvent.sessionId,
                gameId = productEvent.gameId,
                occurredAt = productEvent.occurredAt,
                questId = productEvent.questId,
                slot = productEvent.slot,
                lootableItemId = productEvent.lootableItemId,
                price = productEvent.price,
                unitId = productEvent.unitId,
            )
    }

    fun toProductEventRecorded(): ProductEventRecorded? {
        val name = ProductEventRecorded.Name.entries.firstOrNull { entry -> entry.name == name } ?: return null
        return ProductEventRecorded(
            name = name,
            installId = installId,
            sessionId = sessionId,
            gameId = gameId,
            occurredAt = occurredAt,
            questId = questId,
            slot = slot,
            lootableItemId = lootableItemId,
            price = price,
            unitId = unitId,
        )
    }
}
