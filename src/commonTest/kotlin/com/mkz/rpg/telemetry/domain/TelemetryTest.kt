package com.mkz.rpg.telemetry.domain

import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TelemetryTest {
    private val day = 86_400_000.0

    private fun consented() = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 0.0)

    @Test
    fun `Telemetry should add a product event recorded stamped with every id when consent is given`() {
        // When
        val (events, _) = consented().loadGameId("g1").questCompleted(questId = "bats-in-the-meadow", occurredAt = 1_000.0).pullEvents()
        // Then
        events.shouldContainExactly(
            ProductEventRecorded(
                name = Name.QUEST_COMPLETED,
                installId = "i1",
                sessionId = "s1",
                gameId = "g1",
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
        val (events, _) =
            consented()
                .mainMenuOpened(occurredAt = 1.0)
                .startGame(gameId = "g1", occurredAt = 2.0)
                .itemBought(lootableItemId = "health_potion", price = 10, occurredAt = 3.0)
                .itemSold(lootableItemId = "dagger", sellPrice = 4, occurredAt = 4.0)
                .gameSaved(slot = 1, occurredAt = 5.0)
                .pullEvents()
        // Then
        events.filterIsInstance<ProductEventRecorded>().map { productEvent -> productEvent.name to productEvent.price }.shouldContainExactly(
            Name.MAIN_MENU_OPENED to null,
            Name.GAME_STARTED to null,
            Name.ITEM_BOUGHT to 10,
            Name.ITEM_SOLD to 4,
            Name.GAME_SAVED to null,
        )
    }

    @Test
    fun `Telemetry should add no product event when consent is not asked yet`() {
        // When
        val (events, _) =
            Telemetry
                .notAsked()
                .startSession("s1")
                .battleStarted(occurredAt = 1.0)
                .pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should add no product event when consent is declined`() {
        // When
        val (events, _) = consented().declineConsent(decidedAt = 1.0).gameOver(occurredAt = 2.0).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should add no product event when consent was given on another privacy notice`() {
        // Given
        val dto = consented().toDto()
        val telemetry = Telemetry.read(dto.copy(consent = dto.consent.copy(noticeVersion = Telemetry.PRIVACY_NOTICE_VERSION - 1))).startSession("s1")
        // When
        val (events, _) = telemetry.battleFinished(occurredAt = 1.0).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should forget the events once pulled`() {
        // Given
        val (_, telemetry) = consented().memberRecruited(unitId = "elf", occurredAt = 1.0).pullEvents()
        // When
        val (events, _) = telemetry.pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Telemetry should keep its install id when consent is given again`() {
        // When
        val telemetry = consented().giveConsent(installId = "i2", decidedAt = 1.0)
        // Then
        telemetry.toDto().installId shouldBe "i1"
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

    @Test
    fun `Telemetry should make the game id of a new game when consent is given`() {
        // When
        val (events, telemetry) = consented().startGame(gameId = "g1", occurredAt = 1.0).pullEvents()
        // Then
        events.filterIsInstance<ProductEventRecorded>().single().gameId shouldBe "g1"
        telemetry.toDto().gameId shouldBe "g1"
    }

    @Test
    fun `Telemetry should make no game id when consent is not given`() {
        // When
        val telemetry = Telemetry.notAsked().startSession("s1").startGame(gameId = "g1", occurredAt = 1.0)
        // Then
        telemetry.toDto().gameId.shouldBeNull()
    }

    @Test
    fun `Telemetry should keep the game id of the save when a game is loaded`() {
        // When
        val (events, _) = consented().loadGameId("g1").gameLoaded(slot = 1, newGameId = "g2", occurredAt = 1.0).pullEvents()
        // Then
        events.filterIsInstance<ProductEventRecorded>().single().gameId shouldBe "g1"
    }

    @Test
    fun `Telemetry should take the new game id when the loaded save has none`() {
        // When
        val (events, telemetry) = consented().loadGameId(null).gameLoaded(slot = 1, newGameId = "g2", occurredAt = 1.0).pullEvents()
        // Then
        events.filterIsInstance<ProductEventRecorded>().single().gameId shouldBe "g2"
        telemetry.toDto().gameId shouldBe "g2"
    }

    @Test
    fun `Telemetry should forget the game id when the main menu is opened`() {
        // When
        val (events, telemetry) = consented().startGame(gameId = "g1", occurredAt = 1.0).mainMenuOpened(occurredAt = 2.0).pullEvents()
        // Then
        events
            .filterIsInstance<ProductEventRecorded>()
            .last()
            .gameId
            .shouldBeNull()
        telemetry.toDto().gameId.shouldBeNull()
    }
}
