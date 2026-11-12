package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.quest.domain.QuestEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.telemetry.adapters.presentation.TelemetryApi
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ProductEventDto
import com.mkz.rpg.telemetry.domain.TelemetryEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.random.Random
import kotlin.test.Test

class TelemetryAcceptanceTest {
    private val storage = InmemoryStorage()
    private val remoteTelemetryRepository = RecordingRemoteTelemetryRepository()

    private val everyEvent =
        listOf(
            TelemetryEvent.RequestRecordMainMenuOpened,
            TelemetryEvent.RequestStartGame,
            QuestEvent.QuestStarted(questId = "bats-in-the-meadow"),
            PartyEvent.PartyJoined(playerId = "human", battleUnitId = "elf-1", unitId = "elf"),
            InventoryEvent.ItemBought(battleUnitId = "knight-1", lootableItemId = "health_potion", price = 10),
            InventoryEvent.ItemSold(battleUnitId = "knight-1", lootableItemId = "dagger", sellPrice = 4),
            BattleEvent.BattleStarted,
            BattleEvent.BattleFinished,
            QuestEvent.QuestCompleted(questId = "bats-in-the-meadow"),
            QuestEvent.QuestAbandoned(questId = "rats-in-the-fields"),
            TelemetryEvent.RequestRecordGameSaved(slot = 1),
            PartyEvent.GameOver(playerId = "human"),
            TelemetryEvent.RequestRecordGameLoaded(slot = 1),
        )

    private fun consentGiven() {
        storage["telemetry.consent"] = """{"status":"GIVEN","noticeVersion":${Telemetry.PRIVACY_NOTICE_VERSION},"decidedAt":0.0}"""
        storage["telemetry.installId"] = "i1"
    }

    private fun launch(seed: Int = 1) = TelemetryApi(storage, remoteTelemetryRepository, Random(seed)) { 1_000.0 }

    private fun TelemetryApi.publishOnASceneBus(vararg events: DomainEvent) {
        val eventBus = InMemoryEventBus()
        listen(eventBus)
        events.forEach(eventBus::publish)
        eventBus.dispatch()
    }

    @Test
    fun `Telemetry should record nothing when the player was never asked for consent`() {
        // Given
        val telemetryApi = launch()
        // When
        telemetryApi.publishOnASceneBus(*everyEvent.toTypedArray())
        // Then
        remoteTelemetryRepository.created.shouldBeEmpty()
        storage.keys().shouldBeEmpty()
    }

    @Test
    fun `Telemetry should record a quest completed when Bats in the Meadow is completed with consent`() {
        // Given
        consentGiven()
        val telemetryApi = launch()
        // When
        telemetryApi.publishOnASceneBus(QuestEvent.QuestCompleted(questId = "bats-in-the-meadow"))
        // Then
        val productEvent = remoteTelemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe NameDto.QUEST_COMPLETED
        productEvent.questId shouldBe "bats-in-the-meadow"
        productEvent.installId shouldBe "i1"
        productEvent.occurredAt shouldBe 1_000.0
    }

    @Test
    fun `Telemetry should record every product event the game announces with consent`() {
        // Given
        consentGiven()
        val telemetryApi = launch()
        // When
        telemetryApi.publishOnASceneBus(*everyEvent.toTypedArray())
        // Then
        remoteTelemetryRepository.created.map(ProductEventDto::name).shouldContainExactly(
            NameDto.MAIN_MENU_OPENED,
            NameDto.GAME_STARTED,
            NameDto.QUEST_STARTED,
            NameDto.MEMBER_RECRUITED,
            NameDto.ITEM_BOUGHT,
            NameDto.ITEM_SOLD,
            NameDto.BATTLE_STARTED,
            NameDto.BATTLE_FINISHED,
            NameDto.QUEST_COMPLETED,
            NameDto.QUEST_ABANDONED,
            NameDto.GAME_SAVED,
            NameDto.GAME_OVER,
            NameDto.GAME_LOADED,
        )
    }

    @Test
    fun `Telemetry should stamp every product event of a launch with the same session id`() {
        // Given
        consentGiven()
        val telemetryApi = launch()
        // When
        telemetryApi.publishOnASceneBus(TelemetryEvent.RequestRecordMainMenuOpened)
        telemetryApi.publishOnASceneBus(TelemetryEvent.RequestStartGame, BattleEvent.BattleStarted)
        launch(seed = 2).publishOnASceneBus(TelemetryEvent.RequestRecordMainMenuOpened)
        // Then
        val sessionIds = remoteTelemetryRepository.created.map(ProductEventDto::sessionId)
        sessionIds.shouldHaveSize(4)
        sessionIds.take(3).distinct().shouldHaveSize(1)
        (sessionIds.first() == sessionIds.last()) shouldBe false
        sessionIds.first().length shouldBe 32
    }

    private class RecordingRemoteTelemetryRepository : RemoteTelemetryRepository() {
        val created = mutableListOf<ProductEventDto>()

        override fun create(productEvent: ProductEventDto) {
            created += productEvent
        }
    }
}
