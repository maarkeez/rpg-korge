package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.quest.adapters.presentation.QuestApi
import com.mkz.rpg.quest.adapters.resources.ResourcesQuestLoader
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.domain.QuestMother.reward
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class QuestAcceptanceTest {
    // The game as it ships, read from the resources: the hero, carrying 30 gold, leads the party, and the goat wizard, with 150 health points, is the only goat deployed.
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val lootableItemApi = LootableItemApi(effectApi, eventBus, ResourcesLootableItemLoader())
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus, ResourcesLootTableLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, ResourcesLootDropLoader(), Random.Default)
    private val battleApi = BattleApi(eventBus, playerApi)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    private val questApi = QuestApi(battlefieldApi, partyApi, eventBus, ResourcesQuestLoader())
    private val battleSetupApi = BattleSetupApi(eventBus)

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        lootTableApi.init()
        abilityApi.init()
        unitApi.init()
        lootDropApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        npcApi.init()
        questApi.init()
        battleSetupApi.setupBattle()
        eventBus.dispatch()
    }

    // The goat wizard is hit 15 times for 10 damage, which defeats it.
    private fun defeatTheGoatWizard() =
        repeat(15) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.terrain("lava"),
                    target = EffectTargetDto.battleUnit("goat-wizard"),
                    effectId = "low-physical-damage",
                    destination = null,
                ),
            )
        }

    @Test
    fun `The main quest should be active when a new game is set up`() =
        scenarioTest(::setUp) {
            // When
            val mainQuest = questApi.searchQuests().single()
            // Then
            mainQuest.id shouldBe "defeat-the-goat-wizard"
            mainQuest.progress.status shouldBe StatusDto.ACTIVE
        }

    @Test
    fun `The leader should carry the reward gold when the main quest's unit is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(partyApi.searchParty()!!.leaderBattleUnitId == "hero")
            require(inventoryApi.searchInventoryByBattleUnitId("hero")!!.gold == 30)
            defeatTheGoatWizard()
            // When
            eventBus.dispatch()
            // Then
            questApi
                .searchQuests()
                .single()
                .progress.status shouldBe StatusDto.COMPLETED
            inventoryApi.searchInventoryByBattleUnitId("hero")!!.gold shouldBe 180
        }

    // A small game: a knight explores from (1,0), on a 3x6 field of sand, with a rat at (1,5), and its quest pays 13 health potions, one more than its bag holds.
    private fun smallGame(quest: Quest.Dto): SubdomainApis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(increaseHealthEffect(id = "cure").toDto(), decreaseHealthEffect(id = "smite", damage = 100, applicationType = "IMMEDIATELY").toDto()),
            lootableItems = listOf(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("cure"))).toDto()),
            units = listOf(unit(id = "knight", healthPoints = 30, abilities = emptyList(), movementRange = 3).toDto(), unit(id = "rat", healthPoints = 8, abilities = emptyList()).toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(6) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 0),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 5),
                ),
            quests = listOf(quest),
            explore = true,
        ).also { apis ->
            apis.initialise()
            apis.questApi.startMainQuest()
            apis.eventBus.dispatch()
        }

    private fun SubdomainApis.defeatTheRat() {
        battleUnitApi.applyEffect(
            EffectApplicationDto(
                source = ApplicationSourceDto.terrain("lava"),
                target = EffectTargetDto.battleUnit("rat-1"),
                effectId = "smite",
                destination = null,
            ),
        )
        eventBus.dispatch()
    }

    private fun SubdomainApis.walk(direction: DirectionDto) {
        partyApi.walkParty(direction)
        eventBus.dispatch()
    }

    private val thirteenPotions = reward(gold = 0, lootableItemIds = List(13) { "health-potion" })

    @Test
    fun `A reward item should lie on the leader's tile and be stored when it walks back when the bag is full`() {
        // Given
        val apis = smallGame(mainQuest(tasks = listOf(defeatUnits(unitId = "rat")), reward = thirteenPotions).toDto())
        // When
        apis.defeatTheRat()
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")!!.storedItems shouldBe List(12) { "health-potion" }
        apis.lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 1, column = 0) to listOf("health-potion"))
        // When: it makes room, walks away and walks back
        apis.inventoryApi.discardItem(battleUnitId = "knight-1", lootableItemId = "health-potion")
        apis.eventBus.dispatch()
        apis.walk(DirectionDto.EAST)
        apis.walk(DirectionDto.WEST)
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")!!.storedItems shouldBe List(12) { "health-potion" }
        apis.lootDropApi.searchDroppedItems().shouldBeEmpty()
    }

    @Test
    fun `A reward item should lie on the tile the leader walked onto when reaching it completes the quest`() {
        // Given
        val apis = smallGame(mainQuest(tasks = listOf(reachTiles(1 to 1)), reward = thirteenPotions).toDto())
        // When
        apis.walk(DirectionDto.EAST)
        // Then
        apis.battlefieldApi.searchPosition("knight-1")?.let { position -> position.row to position.column } shouldBe (1 to 1)
        apis.lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 1, column = 1) to listOf("health-potion"))
    }
}
