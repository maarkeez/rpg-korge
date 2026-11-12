package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
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

        val json = Json { ignoreUnknownKeys = false }
    }

    private var sessionId: String? = null

    fun search(): Telemetry? {
        val consent = storage.getOrNull(CONSENT_KEY)?.let(::decodeConsent)
        if (consent == null && sessionId == null) return null
        return Telemetry.read(
            Telemetry.Dto(
                consent = consent ?: ConsentDto(status = ConsentStatusDto.NOT_ASKED, noticeVersion = null, decidedAt = null),
                installId = storage.getOrNull(INSTALL_ID_KEY),
                sessionId = sessionId,
            ),
        )
    }

    fun save(telemetry: Telemetry) {
        val dto = telemetry.toDto()
        storage[CONSENT_KEY] = json.encodeToString(ConsentFile(status = dto.consent.status.name, noticeVersion = dto.consent.noticeVersion, decidedAt = dto.consent.decidedAt))
        val installId = dto.installId
        if (installId == null) storage.remove(INSTALL_ID_KEY) else storage[INSTALL_ID_KEY] = installId
        sessionId = dto.sessionId
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
