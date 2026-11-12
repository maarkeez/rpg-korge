package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.quest.domain.QuestEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.telemetry.adapters.presentation.TelemetryApi
import com.mkz.rpg.telemetry.adapters.storage.RecordingRemoteTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryEvent
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.random.Random
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
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

    private fun launch(seed: Int = 1) = TelemetryApi(storage, remoteTelemetryRepository, TestScope(UnconfinedTestDispatcher()), Random(seed)) { 1_000.0 }

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
    fun `Telemetry should record nothing when the player declined`() {
        // Given
        val telemetryApi = launch()
        telemetryApi.declineConsent()
        // When
        telemetryApi.publishOnASceneBus(*everyEvent.toTypedArray())
        // Then
        remoteTelemetryRepository.created.shouldBeEmpty()
        storage.keys().shouldContainExactly("telemetry.consent")
    }

    @Test
    fun `Telemetry should record a quest completed when Bats in the Meadow is completed with consent`() {
        // Given
        val telemetryApi = launch()
        telemetryApi.giveConsent()
        // When
        telemetryApi.publishOnASceneBus(QuestEvent.QuestCompleted(questId = "bats-in-the-meadow"))
        // Then
        val productEvent = remoteTelemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.QUEST_COMPLETED
        productEvent.questId shouldBe "bats-in-the-meadow"
        productEvent.installId shouldBe storage.getOrNull("telemetry.installId")
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
        remoteTelemetryRepository.created.map(ProductEventRecorded::name).shouldContainExactly(
            Name.MAIN_MENU_OPENED,
            Name.GAME_STARTED,
            Name.QUEST_STARTED,
            Name.MEMBER_RECRUITED,
            Name.ITEM_BOUGHT,
            Name.ITEM_SOLD,
            Name.BATTLE_STARTED,
            Name.BATTLE_FINISHED,
            Name.QUEST_COMPLETED,
            Name.QUEST_ABANDONED,
            Name.GAME_SAVED,
            Name.GAME_OVER,
            Name.GAME_LOADED,
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
        val sessionIds = remoteTelemetryRepository.created.map(ProductEventRecorded::sessionId)
        sessionIds.shouldHaveSize(4)
        sessionIds.take(3).distinct().shouldHaveSize(1)
        (sessionIds.first() == sessionIds.last()) shouldBe false
        sessionIds.first().length shouldBe 32
    }

    @Test
    fun `Telemetry should send at the next launch what the remote did not accept`() {
        // Given
        consentGiven()
        remoteTelemetryRepository.isAccepting = false
        launch().publishOnASceneBus(QuestEvent.QuestCompleted(questId = "bats-in-the-meadow"))
        remoteTelemetryRepository.isAccepting = true
        // When
        launch(seed = 2)
        // Then
        remoteTelemetryRepository.batches.map { batch -> batch.map(ProductEventRecorded::name) }.shouldContainExactly(
            listOf(Name.QUEST_COMPLETED),
            listOf(Name.QUEST_COMPLETED),
        )
        storage.getOrNull("telemetry.productEvents").shouldBeNull()
    }
}
