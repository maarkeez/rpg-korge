package com.mkz.rpg.save.adapters.json

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.test.Test

/**
 * The saves of each version, kept as golden fixtures under src/jvmTest/fixtures/save: the game saved with
 * [Save.SAVE_VERSION] must still be saved as `v<SAVE_VERSION>.json`, and load from it, so a change to the shape of a
 * snapshot is noticed. Then bump [Save.SAVE_VERSION], and write the new fixture, keeping the older ones:
 * `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`.
 */
class SaveFixtureTest {
    private val fixtures = File("src/jvmTest/fixtures/save")
    private val currentFixture = File(fixtures, "v${Save.SAVE_VERSION}.json")
    private val isUpdating = System.getProperty("rpg.updateSaveFixtures") == "true"
    private val prettyJson = Json { prettyPrint = true }

    // The game of the fixture, as read from the resources: a 3x6 sand field split by a column of void, a knight carrying
    // 40 gold, and a rat that drops health potions. Played: the knight explores from (1,0), the elder at (0,3) sells
    // health potions, a rat wanders from its home at (2,5), past the void, out of the knight's reach, and a health potion
    // lies at (1,1). Nothing is random, so the game is saved the same every time.
    private class Game(
        isPlayed: Boolean,
    ) {
        val apis =
            SubdomainApis(
                terrains = listOf(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("void")).toDto(), nonOccupiableTerrain(id = "void").toDto()),
                effects = listOf(increaseHealthEffect(id = "cure").toDto()),
                lootableItems = listOf(consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("cure"))).toDto()),
                lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(lootableItemEntry(lootableItemId = "health-potion"), nothingEntry())).toDto()),
                units =
                    listOf(
                        unit(id = "knight", healthPoints = 30, manaPoints = 10, abilities = emptyList(), movementRange = 3, startingGold = 40).toDto(),
                        unit(id = "rat", healthPoints = 8, manaPoints = 0, abilities = emptyList(), movementRange = 1).toDto(),
                    ),
                lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
                players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
                tiles = List(3) { listOf("sand", "sand", "sand", "sand", "void", "sand") },
                deployments =
                    if (isPlayed) {
                        listOf(
                            Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 0),
                            Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 2, column = 5),
                        )
                    } else {
                        emptyList()
                    },
                npcs = if (isPlayed) listOf(npc(id = "elder", row = 0, column = 3, stock = listOf("health-potion")).toDto()) else emptyList(),
                explore = isPlayed,
            )
        private val cpuBattleBrainApi =
            CpuBattleBrainApi(apis.unitApi, apis.playerApi, apis.battleUnitApi, apis.battlefieldApi, apis.terrainApi, apis.effectApi, apis.abilityApi, apis.eventBus, FixedDoublesRandom(0.0))
        val cpuWanderBrainApi = CpuWanderBrainApi(apis.playerApi, apis.battleUnitApi, apis.battlefieldApi, cpuBattleBrainApi, apis.partyApi, apis.eventBus)
        val storage = InmemoryStorage()
        val saveApi =
            SaveApi(
                storage = storage,
                eventBus = apis.eventBus,
                snapshottables =
                    listOf(
                        apis.battleUnitApi.snapshottable,
                        apis.battlefieldApi.snapshottable,
                        apis.npcApi.snapshottable,
                        apis.lootDropApi.snapshottable,
                        apis.inventoryApi.snapshottable,
                        apis.partyApi.snapshottable,
                        cpuWanderBrainApi.snapshottable,
                    ),
                canSave = { apis.partyApi.searchParty()?.isPaused == true },
                leaderUnitId = { "knight" },
                partySize = {
                    apis.partyApi
                        .searchParty()
                        ?.battleUnitIds
                        ?.size ?: 0
                },
                now = { 1_793_385_720_000.0 },
            )

        init {
            apis.initialise()
        }

        // The knight walks east onto the health potion, which it stores, then the game is saved in slot 1.
        fun playAndSave(): String {
            apis.lootDropApi.snapshottable.restore("""[{"unitId":"rat","droppedItems":[{"row":1,"column":1,"lootableItemIds":["health-potion"]}]}]""")
            apis.partyApi.walkParty(DirectionDto.EAST)
            apis.eventBus.dispatch()
            apis.partyApi.openPause()
            apis.eventBus.dispatch()
            saveApi.saveGame(slot = 1)
            return requireNotNull(storage.getOrNull("save.slot1"))
        }

        fun load(fixture: File) {
            val save = Json.decodeFromString<SaveFile>(fixture.readText())
            storage["save.slot1"] = SaveFile.json.encodeToString(SaveFile.serializer(), save)
            storage["save.slot1.summary"] = SaveFile.json.encodeToString(SaveSummaryFile.serializer(), save.summary)
            saveApi.loadGame(slot = 1)
            apis.eventBus.dispatch()
        }
    }

    private fun json(text: String): JsonElement = Json.parseToJsonElement(text)

    @Test
    fun `SaveGame should save the fixture of SAVE_VERSION when the game is played as it was`() {
        // When
        val saved = Game(isPlayed = true).playAndSave()
        // Then
        if (isUpdating) currentFixture.writeText(prettyJson.encodeToString(JsonElement.serializer(), json(saved)) + "\n")
        withClue("The save differs from ${currentFixture.path}: bump Save.SAVE_VERSION and write the new fixture with -Drpg.updateSaveFixtures=true") {
            json(saved) shouldBe json(currentFixture.readText())
        }
    }

    @Test
    fun `SaveFile should encode the fixture as it was when it decodes the fixture of SAVE_VERSION`() {
        // Given
        val fixture = currentFixture.readText()
        // When
        val encoded = SaveFile.json.encodeToString(SaveFile.serializer(), SaveFile.json.decodeFromString(SaveFile.serializer(), fixture))
        // Then
        json(encoded) shouldBe json(fixture)
    }

    @Test
    fun `LoadGame should restore the fixture's leader tile, items and gold when it loads the fixture of SAVE_VERSION`() {
        // Given
        val game = Game(isPlayed = false)
        // When
        game.load(currentFixture)
        // Then
        game.apis.battlefieldApi
            .searchPosition("knight-1")
            ?.let { position -> position.row to position.column } shouldBe (1 to 1)
        val inventory = requireNotNull(game.apis.inventoryApi.searchInventoryByBattleUnitId("knight-1"))
        inventory.storedItems.shouldContainExactly("health-potion")
        inventory.gold shouldBe 40
        game.apis.partyApi
            .searchParty()
            ?.battleUnitIds
            .orEmpty()
            .shouldContainExactly("knight-1")
        game.apis.npcApi
            .searchNpcById("elder")
            ?.stock
            .orEmpty()
            .shouldContainExactly("health-potion")
        game.cpuWanderBrainApi.snapshottable.snapshot() shouldBe Game(isPlayed = true).cpuWanderBrainApi.snapshottable.snapshot()
    }

    @Test
    fun `LoadGame should refuse when it loads the fixture of an older version`() {
        // Given
        val game = Game(isPlayed = false)
        // When
        val result = runCatching { game.load(File(fixtures, "v0.json")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<SaveError.IncompatibleSave>()
    }
}
