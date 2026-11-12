package com.mkz.rpg.telemetry.domain

import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Telemetry private constructor(
    private val consent: Consent,
    private val installId: InstallId?,
    private val sessionId: SessionId?,
    private val gameId: GameId?,
    private val productEvents: List<ProductEvent>,
) {
    companion object {
        const val PRIVACY_NOTICE_VERSION = 1

        const val CONSENT_ASKED_AGAIN_AFTER_DAYS = 180

        private const val MILLISECONDS_PER_DAY = 86_400_000.0

        fun notAsked(): Telemetry =
            Telemetry(
                consent = Consent(status = ConsentStatusDto.NOT_ASKED, noticeVersion = null, decidedAt = null),
                installId = null,
                sessionId = null,
                gameId = null,
                productEvents = emptyList(),
            )

        fun read(dto: Dto): Telemetry =
            Telemetry(
                consent = Consent(status = dto.consent.status, noticeVersion = dto.consent.noticeVersion, decidedAt = dto.consent.decidedAt),
                installId = dto.installId?.let(::InstallId),
                sessionId = dto.sessionId?.let(::SessionId),
                gameId = dto.gameId?.let(::GameId),
                productEvents = emptyList(),
            )
    }

    fun toDto() =
        Dto(
            consent = Dto.ConsentDto(status = consent.status, noticeVersion = consent.noticeVersion, decidedAt = consent.decidedAt),
            installId = installId?.value,
            sessionId = sessionId?.value,
            gameId = gameId?.value,
        )

    fun startSession(sessionId: String): Telemetry = copy(sessionId = SessionId(sessionId))

    fun giveConsent(
        installId: String,
        decidedAt: Double,
    ): Telemetry =
        copy(
            consent = Consent(status = ConsentStatusDto.GIVEN, noticeVersion = PRIVACY_NOTICE_VERSION, decidedAt = decidedAt),
            installId = this.installId ?: InstallId(installId),
        )

    fun declineConsent(decidedAt: Double): Telemetry =
        copy(
            consent = Consent(status = ConsentStatusDto.DECLINED, noticeVersion = PRIVACY_NOTICE_VERSION, decidedAt = decidedAt),
            installId = null,
            gameId = null,
        )

    fun isConsentAsked(now: Double): Boolean =
        when {
            consent.status == ConsentStatusDto.NOT_ASKED -> true
            consent.noticeVersion != PRIVACY_NOTICE_VERSION -> true
            consent.status == ConsentStatusDto.DECLINED -> now - (consent.decidedAt ?: 0.0) >= CONSENT_ASKED_AGAIN_AFTER_DAYS * MILLISECONDS_PER_DAY
            else -> false
        }

    fun loadGameId(gameId: String?): Telemetry = copy(gameId = gameId?.let(::GameId))

    fun mainMenuOpened(occurredAt: Double): Telemetry = copy(gameId = null).record(NameDto.MAIN_MENU_OPENED, occurredAt)

    fun startGame(
        gameId: String,
        occurredAt: Double,
    ): Telemetry {
        if (!isConsentGiven()) return this
        return copy(gameId = GameId(gameId)).record(NameDto.GAME_STARTED, occurredAt)
    }

    fun gameLoaded(
        slot: Int,
        newGameId: String,
        occurredAt: Double,
    ): Telemetry {
        if (!isConsentGiven()) return this
        return copy(gameId = gameId ?: GameId(newGameId)).record(NameDto.GAME_LOADED, occurredAt, Details(slot = slot))
    }

    fun gameSaved(
        slot: Int,
        occurredAt: Double,
    ): Telemetry = record(NameDto.GAME_SAVED, occurredAt, Details(slot = slot))

    fun questStarted(
        questId: String,
        occurredAt: Double,
    ): Telemetry = record(NameDto.QUEST_STARTED, occurredAt, Details(questId = questId))

    fun questCompleted(
        questId: String,
        occurredAt: Double,
    ): Telemetry = record(NameDto.QUEST_COMPLETED, occurredAt, Details(questId = questId))

    fun questAbandoned(
        questId: String,
        occurredAt: Double,
    ): Telemetry = record(NameDto.QUEST_ABANDONED, occurredAt, Details(questId = questId))

    fun battleStarted(occurredAt: Double): Telemetry = record(NameDto.BATTLE_STARTED, occurredAt)

    fun battleFinished(occurredAt: Double): Telemetry = record(NameDto.BATTLE_FINISHED, occurredAt)

    fun gameOver(occurredAt: Double): Telemetry = record(NameDto.GAME_OVER, occurredAt)

    fun itemBought(
        lootableItemId: String,
        price: Int,
        occurredAt: Double,
    ): Telemetry = record(NameDto.ITEM_BOUGHT, occurredAt, Details(lootableItemId = lootableItemId, price = price))

    fun itemSold(
        lootableItemId: String,
        sellPrice: Int,
        occurredAt: Double,
    ): Telemetry = record(NameDto.ITEM_SOLD, occurredAt, Details(lootableItemId = lootableItemId, price = sellPrice))

    fun memberRecruited(
        unitId: String,
        occurredAt: Double,
    ): Telemetry = record(NameDto.MEMBER_RECRUITED, occurredAt, Details(unitId = unitId))

    fun pullProductEvents(): Pair<List<Dto.ProductEventDto>, Telemetry> = productEvents.map(ProductEvent::toDto) to copy(productEvents = emptyList())

    private fun record(
        name: NameDto,
        occurredAt: Double,
        details: Details = Details(),
    ): Telemetry {
        val installId = installId ?: return this
        val sessionId = sessionId ?: return this
        if (!isConsentGiven()) return this
        return copy(productEvents = productEvents + ProductEvent(name, installId, sessionId, gameId, occurredAt, details))
    }

    private fun isConsentGiven() = consent.status == ConsentStatusDto.GIVEN && consent.noticeVersion == PRIVACY_NOTICE_VERSION

    private data class Consent(
        val status: ConsentStatusDto,
        val noticeVersion: Int?,
        val decidedAt: Double?,
    )

    @JvmInline private value class InstallId(
        val value: String,
    )

    @JvmInline private value class SessionId(
        val value: String,
    )

    @JvmInline private value class GameId(
        val value: String,
    )

    private data class Details(
        val questId: String? = null,
        val slot: Int? = null,
        val lootableItemId: String? = null,
        val price: Int? = null,
        val unitId: String? = null,
    )

    private data class ProductEvent(
        val name: NameDto,
        val installId: InstallId,
        val sessionId: SessionId,
        val gameId: GameId?,
        val occurredAt: Double,
        val details: Details,
    ) {
        fun toDto() =
            Dto.ProductEventDto(
                name = name,
                installId = installId.value,
                sessionId = sessionId.value,
                gameId = gameId?.value,
                occurredAt = occurredAt,
                questId = details.questId,
                slot = details.slot,
                lootableItemId = details.lootableItemId,
                price = details.price,
                unitId = details.unitId,
            )
    }

    data class Dto(
        val consent: ConsentDto,
        val installId: String?,
        val sessionId: String?,
        val gameId: String?,
    ) {
        data class ConsentDto(
            val status: ConsentStatusDto,
            val noticeVersion: Int?,
            val decidedAt: Double?,
        )

        enum class ConsentStatusDto {
            NOT_ASKED,
            GIVEN,
            DECLINED,
        }

        data class ProductEventDto(
            val name: NameDto,
            val installId: String,
            val sessionId: String,
            val gameId: String?,
            val occurredAt: Double,
            val questId: String?,
            val slot: Int?,
            val lootableItemId: String?,
            val price: Int?,
            val unitId: String?,
        )

        enum class NameDto {
            MAIN_MENU_OPENED,
            GAME_STARTED,
            GAME_LOADED,
            GAME_SAVED,
            QUEST_STARTED,
            QUEST_COMPLETED,
            QUEST_ABANDONED,
            BATTLE_STARTED,
            BATTLE_FINISHED,
            GAME_OVER,
            ITEM_BOUGHT,
            ITEM_SOLD,
            MEMBER_RECRUITED,
        }
    }
}
