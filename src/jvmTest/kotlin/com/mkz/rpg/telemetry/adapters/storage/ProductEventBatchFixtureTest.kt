package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.test.Test

class ProductEventBatchFixtureTest {
    private companion object {
        const val INSTALL_ID = "0f1e2d3c4b5a69788796a5b4c3d2e1f0"
        const val SESSION_ID = "a1b2c3d4e5f60718293a4b5c6d7e8f90"
        const val GAME_ID = "00112233445566778899aabbccddeeff"
        const val STARTED_AT = 1_794_607_613_037.0
    }

    private val fixture = File("src/jvmTest/fixtures/telemetry/product-events.json")
    private val isUpdating = System.getProperty("rpg.updateTelemetryFixtures") == "true"
    private val prettyJson = Json { prettyPrint = true }

    @Test
    fun `The sample batch should be what the game sends for the 13 product events`() {
        // Given
        var occurredAt = STARTED_AT
        val (events, _) =
            Telemetry
                .notAsked()
                .startSession(SESSION_ID)
                .giveConsent(installId = INSTALL_ID, decidedAt = STARTED_AT)
                .mainMenuOpened(occurredAt++)
                .startGame(gameId = GAME_ID, occurredAt = occurredAt++)
                .gameSaved(slot = 1, occurredAt = occurredAt++)
                .gameLoaded(slot = 1, newGameId = GAME_ID, occurredAt = occurredAt++)
                .questStarted(questId = "bats-in-the-meadow", occurredAt = occurredAt++)
                .battleStarted(occurredAt++)
                .battleFinished(occurredAt++)
                .questCompleted(questId = "bats-in-the-meadow", occurredAt = occurredAt++)
                .itemBought(lootableItemId = "health-potion", price = 10, occurredAt = occurredAt++)
                .itemSold(lootableItemId = "bee-essence", sellPrice = 3, occurredAt = occurredAt++)
                .memberRecruited(unitId = "viking-warrior", occurredAt = occurredAt++)
                .questAbandoned(questId = "rats-in-the-fields", occurredAt = occurredAt++)
                .gameOver(occurredAt)
                .pullEvents()
        val productEvents = events.filterIsInstance<ProductEventRecorded>()
        // When
        val batch = ProductEventJson.encodeBatch(productEvents)
        // Then
        productEvents.map(ProductEventRecorded::name).shouldContainExactlyInAnyOrder(ProductEventRecorded.Name.entries)
        if (isUpdating) fixture.writeText(prettyJson.encodeToString(JsonElement.serializer(), json(batch)) + "\n")
        withClue("The batch the game sends differs from ${fixture.path}: write it again with -Drpg.updateTelemetryFixtures=true, and update the telemetry collector to read it") {
            json(batch) shouldBe json(fixture.readText())
        }
    }

    private fun json(text: String): JsonElement = Json.parseToJsonElement(text)
}
