package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import korlibs.korge.service.storage.IStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NativeStorageTelemetryRepository(
    private val storage: IStorage,
) {
    private companion object {
        const val CONSENT_KEY = "telemetry.consent"
        const val INSTALL_ID_KEY = "telemetry.installId"
        const val PRODUCT_EVENTS_KEY = "telemetry.productEvents"

        val json = Json { ignoreUnknownKeys = false }
    }

    private var sessionId: String? = null
    private var gameId: String? = null

    fun search(): Telemetry? {
        val consent = storage.getOrNull(CONSENT_KEY)?.let(::decodeConsent)
        if (consent == null && sessionId == null && gameId == null) return null
        return Telemetry.read(
            Telemetry.Dto(
                consent = consent ?: ConsentDto(status = ConsentStatusDto.NOT_ASKED, noticeVersion = null, decidedAt = null),
                installId = storage.getOrNull(INSTALL_ID_KEY),
                sessionId = sessionId,
                gameId = gameId,
            ),
        )
    }

    fun save(telemetry: Telemetry) {
        val dto = telemetry.toDto()
        if (dto.consent.status == ConsentStatusDto.NOT_ASKED) {
            storage.remove(CONSENT_KEY)
        } else {
            storage[CONSENT_KEY] = json.encodeToString(ConsentFile(status = dto.consent.status.name, noticeVersion = dto.consent.noticeVersion, decidedAt = dto.consent.decidedAt))
        }
        val installId = dto.installId
        if (installId == null) storage.remove(INSTALL_ID_KEY) else storage[INSTALL_ID_KEY] = installId
        sessionId = dto.sessionId
        gameId = dto.gameId
    }

    fun queue(productEvents: List<ProductEventRecorded>) {
        if (productEvents.isEmpty()) return
        write(searchQueued() + productEvents)
    }

    fun searchQueued(): List<ProductEventRecorded> = storage.getOrNull(PRODUCT_EVENTS_KEY)?.let(::decodeProductEvents) ?: emptyList()

    fun removeQueued(productEvents: List<ProductEventRecorded>) {
        val queued = searchQueued().toMutableList()
        productEvents.forEach(queued::remove)
        write(queued)
    }

    fun deleteQueued() {
        storage.remove(PRODUCT_EVENTS_KEY)
    }

    private fun write(productEvents: List<ProductEventRecorded>) {
        if (productEvents.isEmpty()) {
            storage.remove(PRODUCT_EVENTS_KEY)
        } else {
            storage[PRODUCT_EVENTS_KEY] = json.encodeToString(productEvents.map(ProductEventFile::of))
        }
    }

    private fun decodeProductEvents(file: String): List<ProductEventRecorded>? =
        try {
            json.decodeFromString<List<ProductEventFile>>(file).mapNotNull(ProductEventFile::toProductEventRecorded)
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun decodeConsent(file: String): ConsentDto? =
        try {
            val consent = json.decodeFromString<ConsentFile>(file)
            ConsentDto(status = enumValueOf<ConsentStatusDto>(consent.status), noticeVersion = consent.noticeVersion, decidedAt = consent.decidedAt)
        } catch (_: IllegalArgumentException) {
            null
        }
}

@Serializable
private data class ConsentFile(
    val status: String,
    val noticeVersion: Int?,
    val decidedAt: Double?,
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
