package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.quest.adapters.presentation.QuestApi
import com.mkz.tessavar.quest.adapters.resources.ResourcesQuestLoader
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestMother.defeat
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.reward
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.domain.QuestMother.talk
import com.mkz.tessavar.quest.domain.QuestMother.visit
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.adapters.presentation.SubdomainApis
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class QuestAcceptanceTest {
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
    private val battleApi = BattleApi(eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    private val questApi = QuestApi(battlefieldApi, partyApi, eventBus, ResourcesQuestLoader())
    private val gameSetupApi = GameSetupApi(eventBus)

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
        gameSetupApi.setUpGame()
        eventBus.dispatch()
    }

    private fun defeatTheGoatWizard() =
        repeat(15) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = SourceDto.terrain("lava"),
                    recipient = RecipientDto.battleUnit("goat-wizard"),
                    effectId = "low-physical-damage",
                    destination = null,
                ),
            )
        }

    private fun teleportTheHero(
        row: Int,
        column: Int,
    ) {
        battleUnitApi.applyEffect(
            EffectApplicationDto(
                source = SourceDto.terrain("lava"),
                recipient = RecipientDto.battleUnit("hero"),
                effectId = "teleport",
                destination = RecipientDto.position(row = row, column = column),
            ),
        )
        eventBus.dispatch()
    }

    private fun talkToTheElder() {
        teleportTheHero(row = 14, column = 10)
        teleportTheHero(row = 16, column = 9)
        npcApi.talkToAdjacentNpc()
        eventBus.dispatch()
        partyApi.finishTalk()
        eventBus.dispatch()
    }

    private fun mainQuest() = questApi.searchQuests().first { quest -> quest.type == QuestTypeDto.MAIN }

    @Test
    fun `The main quest should not be started when a new game is set up`() =
        scenarioTest(::setUp) {
            // When
            val mainQuest = mainQuest()
            // Then
            mainQuest.id shouldBe "defeat-the-goat-wizard"
            mainQuest.progress.status shouldBe StatusDto.NOT_STARTED
        }

    @Test
    fun `The main quest should start when the leader walks onto the village entrance`() =
        scenarioTest(::setUp) {
            // When
            teleportTheHero(row = 14, column = 10)
            // Then
            mainQuest().progress.status shouldBe StatusDto.ACTIVE
            mainQuest().progress.currentTaskIndex shouldBe 0
        }

    @Test
    fun `The main quest should move on to defeating the Goat Wizard when the leader talks to the Elder`() =
        scenarioTest(::setUp) {
            // When
            talkToTheElder()
            // Then
            mainQuest().progress.status shouldBe StatusDto.ACTIVE
            mainQuest().progress.currentTaskIndex shouldBe 1
        }

    @Test
    fun `The leader should carry the reward gold when the main quest's unit is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(partyApi.searchParty()!!.leaderBattleUnitId == "hero")
            require(inventoryApi.searchInventoryByBattleUnitId("hero")!!.gold == 30)
            talkToTheElder()
            defeatTheGoatWizard()
            // When
            eventBus.dispatch()
            // Then
            questApi
                .searchQuests()
                .first { quest -> quest.type == QuestTypeDto.MAIN }
                .progress.status shouldBe StatusDto.COMPLETED
            inventoryApi.searchInventoryByBattleUnitId("hero")!!.gold shouldBe 180
        }

    private fun smallGame(vararg quests: Quest.Dto): SubdomainApis =
        SubdomainApis(
            terrains = listOf(terrainThatCanBeOccupied(id = "sand").toDto()),
            effects = listOf(healEffect(id = "cure").toDto(), damageEffect(id = "smite", damage = 100, timingType = "IMMEDIATELY").toDto()),
            lootableItems = listOf(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("cure"))).toDto()),
            units =
                listOf(
                    unit(id = "knight", baseMaximumHealth = 30, startingAbilities = emptyList(), maximumSteps = 3).toDto(),
                    unit(id = "rat", baseMaximumHealth = 8, startingAbilities = emptyList()).toDto(),
                ),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(6) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 0),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 5),
                ),
            npcs = listOf(npc(id = "farmer", row = 0, column = 0).toDto()),
            quests = quests.toList(),
            explore = true,
        ).also { apis ->
            apis.initialise()
            apis.questApi.startMainQuest(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 0)))
            apis.eventBus.dispatch()
        }

    private fun SubdomainApis.defeatTheRat() {
        battleUnitApi.applyEffect(
            EffectApplicationDto(
                source = SourceDto.terrain("lava"),
                recipient = RecipientDto.battleUnit("rat-1"),
                effectId = "smite",
                destination = null,
            ),
        )
        eventBus.dispatch()
    }

    private fun SubdomainApis.walk(direction: DirectionDto) {
        partyApi.stepParty(direction)
        eventBus.dispatch()
    }

    private fun SubdomainApis.talkToTheFarmer() {
        npcApi.talkToAdjacentNpc()
        eventBus.dispatch()
        partyApi.finishTalk()
        eventBus.dispatch()
    }

    private val thirteenPotions = reward(gold = 0, lootableItemIds = List(13) { "health-potion" })

    @Test
    fun `A reward item should lie on the leader's tile and be stored when it walks back when the inventory is full`() {
        // Given
        val apis = smallGame(mainQuest(tasks = listOf(defeat(unitId = "rat")), reward = thirteenPotions).toDto())
        // When
        apis.defeatTheRat()
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")!!.storedItems shouldBe List(12) { "health-potion" }
        apis.lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 1, column = 0) to listOf("health-potion"))
        // When
        apis.inventoryApi.discardItem(battleUnitId = "knight-1", lootableItemId = "health-potion")
        apis.eventBus.dispatch()
        apis.walk(DirectionDto.EAST)
        apis.walk(DirectionDto.WEST)
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")!!.storedItems shouldBe List(12) { "health-potion" }
        apis.lootDropApi.searchDroppedItems().shouldBeEmpty()
    }

    @Test
    fun `A reward item should lie on the tile the leader walked onto when visiting it completes the quest`() {
        // Given
        val apis = smallGame(mainQuest(tasks = listOf(visit(1 to 1)), reward = thirteenPotions).toDto())
        // When
        apis.walk(DirectionDto.EAST)
        // Then
        apis.battlefieldApi.searchPosition("knight-1")?.let { position -> position.row to position.column } shouldBe (1 to 1)
        apis.lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 1, column = 1) to listOf("health-potion"))
    }

    @Test
    fun `A side quest should run through its tasks and grant its reward when it is started by talking to its quest giver`() {
        // Given
        val sideQuest =
            sideQuest(
                id = "help-the-farmer",
                questGiverNpcId = "farmer",
                repeatable = true,
                tasks = listOf(visit(2 to 0), talk(npcId = "farmer")),
                reward = reward(gold = 20, lootableItemIds = listOf("health-potion")),
            ).toDto()
        val apis = smallGame(mainQuest(tasks = listOf(defeat(unitId = "rat"))).toDto(), sideQuest)

        fun progress() =
            apis.questApi
                .searchQuests()
                .single { quest -> quest.id == "help-the-farmer" }
                .progress

        fun knight() = apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")!!
        val startingGold = knight().gold
        // When
        apis.talkToTheFarmer()
        // Then
        progress().status shouldBe StatusDto.ACTIVE
        progress().currentTaskIndex shouldBe 0
        // When
        apis.walk(DirectionDto.SOUTH)
        apis.walk(DirectionDto.NORTH)
        // Then
        progress().currentTaskIndex shouldBe 1
        // When
        apis.talkToTheFarmer()
        // Then
        progress().status shouldBe StatusDto.COMPLETED
        knight().gold shouldBe startingGold + 20
        knight().storedItems shouldBe listOf("health-potion")
        // When
        apis.talkToTheFarmer()
        // Then
        progress().status shouldBe StatusDto.ACTIVE
        progress().currentTaskIndex shouldBe 0
    }

    @Test
    fun `An npc should do its talk task in one talk and start the side quest it gives in the next`() {
        // Given
        val apis =
            smallGame(
                mainQuest(id = "main", tasks = listOf(talk(npcId = "farmer"), defeat(unitId = "rat"))).toDto(),
                sideQuest(id = "help-the-farmer", questGiverNpcId = "farmer", tasks = listOf(visit(2 to 0))).toDto(),
            )

        fun progress(questId: String) =
            apis.questApi
                .searchQuests()
                .single { quest -> quest.id == questId }
                .progress
        // When
        apis.talkToTheFarmer()
        // Then
        progress("main").currentTaskIndex shouldBe 1
        progress("help-the-farmer").status shouldBe StatusDto.NOT_STARTED
        // When
        apis.talkToTheFarmer()
        // Then
        progress("main").currentTaskIndex shouldBe 1
        progress("help-the-farmer").status shouldBe StatusDto.ACTIVE
    }
}
