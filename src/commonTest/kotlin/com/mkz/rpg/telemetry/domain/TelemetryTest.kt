package com.mkz.rpg.telemetry.domain

import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ProductEventDto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TelemetryTest {
    private val day = 86_400_000.0

    private fun consented() = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 0.0)

    @Test
    fun `Telemetry should add a product event stamped with every id when consent is given`() {
        // When
        val (productEvents, _) = consented().questCompleted(questId = "bats-in-the-meadow", occurredAt = 1_000.0).pullProductEvents()
        // Then
        productEvents.shouldContainExactly(
            ProductEventDto(
                name = NameDto.QUEST_COMPLETED,
                installId = "i1",
                sessionId = "s1",
                occurredAt = 1_000.0,
                questId = "bats-in-the-meadow",
                slot = null,
                lootableItemId = null,
                price = null,
                unitId = null,
            ),
        )
    }

    @Test
    fun `Telemetry should add every product event in the order they happened when consent is given`() {
        // When
        val (productEvents, _) =
            consented()
                .mainMenuOpened(occurredAt = 1.0)
                .startGame(occurredAt = 2.0)
                .itemBought(lootableItemId = "health_potion", price = 10, occurredAt = 3.0)
                .itemSold(lootableItemId = "dagger", sellPrice = 4, occurredAt = 4.0)
                .gameSaved(slot = 1, occurredAt = 5.0)
                .pullProductEvents()
        // Then
        productEvents.map { productEvent -> productEvent.name to productEvent.price }.shouldContainExactly(
            NameDto.MAIN_MENU_OPENED to null,
            NameDto.GAME_STARTED to null,
            NameDto.ITEM_BOUGHT to 10,
            NameDto.ITEM_SOLD to 4,
            NameDto.GAME_SAVED to null,
        )
    }

    @Test
    fun `Telemetry should add no product event when consent is not asked yet`() {
        // When
        val (productEvents, _) =
            Telemetry
                .notAsked()
                .startSession("s1")
                .battleStarted(occurredAt = 1.0)
                .pullProductEvents()
        // Then
        productEvents.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should add no product event when consent is declined`() {
        // When
        val (productEvents, _) = consented().declineConsent(decidedAt = 1.0).gameOver(occurredAt = 2.0).pullProductEvents()
        // Then
        productEvents.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should add no product event when consent was given on another privacy notice`() {
        // Given
        val dto = consented().toDto()
        val telemetry = Telemetry.read(dto.copy(consent = dto.consent.copy(noticeVersion = Telemetry.PRIVACY_NOTICE_VERSION - 1))).startSession("s1")
        // When
        val (productEvents, _) = telemetry.battleFinished(occurredAt = 1.0).pullProductEvents()
        // Then
        productEvents.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should forget the product events once pulled`() {
        // Given
        val (_, telemetry) = consented().memberRecruited(unitId = "elf", occurredAt = 1.0).pullProductEvents()
        // When
        val (productEvents, _) = telemetry.pullProductEvents()
        // Then
        productEvents.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should forget the install id when consent is withdrawn`() {
        // When
        val telemetry = consented().declineConsent(decidedAt = 1.0)
        // Then
        telemetry.toDto().installId.shouldBeNull()
    }

    @Test
    fun `Telemetry should ask for consent when it was never asked`() {
        // When
        val isAsked = Telemetry.notAsked().isConsentAsked(now = 0.0)
        // Then
        isAsked shouldBe true
    }

    @Test
    fun `Telemetry should not ask for consent when it was given on this privacy notice`() {
        // When
        val isAsked = consented().isConsentAsked(now = 1_000 * day)
        // Then
        isAsked shouldBe false
    }

    @Test
    fun `Telemetry should ask for consent when the privacy notice version changed`() {
        // Given
        val dto = consented().toDto()
        val telemetry = Telemetry.read(dto.copy(consent = dto.consent.copy(noticeVersion = Telemetry.PRIVACY_NOTICE_VERSION - 1)))
        // When
        val isAsked = telemetry.isConsentAsked(now = 1.0)
        // Then
        isAsked shouldBe true
    }

    @Test
    fun `Telemetry should ask for consent again when it was declined 180 days ago`() {
        // When
        val isAsked = Telemetry.notAsked().declineConsent(decidedAt = 0.0).isConsentAsked(now = Telemetry.CONSENT_ASKED_AGAIN_AFTER_DAYS * day)
        // Then
        isAsked shouldBe true
    }

    @Test
    fun `Telemetry should not ask for consent when it was declined 179 days ago`() {
        // When
        val isAsked = Telemetry.notAsked().declineConsent(decidedAt = 0.0).isConsentAsked(now = (Telemetry.CONSENT_ASKED_AGAIN_AFTER_DAYS - 1) * day)
        // Then
        isAsked shouldBe false
    }

    @Test
    fun `Telemetry should read back the consent, the install id and the session id of its dto`() {
        // Given
        val dto = consented().toDto()
        // When
        val telemetry = Telemetry.read(dto)
        // Then
        telemetry.toDto() shouldBe dto
    }
}
