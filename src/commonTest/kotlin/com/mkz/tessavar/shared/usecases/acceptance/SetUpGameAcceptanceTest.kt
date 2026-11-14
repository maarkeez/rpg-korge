package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class SetUpGameAcceptanceTest {
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

    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom.cpuBattleUnitsAlwaysResting())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)
    private val gameSetupApi = GameSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "hero"

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
        gameSetupApi.setUpGame()
        eventBus.dispatch()
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, "player-one", "player-two")
        eventBus.dispatch()
    }

    @Test
    fun `should occupy adjacent tile when battle unit was moved`() =
        scenarioTest(::setUp) {
            // Given
            val currentPosition = battlefieldApi.searchPosition(playerOneKnightBattleUnitId)!!
            require(currentPosition.row == 2 && currentPosition.column == 1)
            require(battleApi.searchBattle()!!.currentPlayerId == playerOneId)
            battleUnitApi.walkBattleUnitInBattle(
                battleUnitId = playerOneKnightBattleUnitId,
                walkToRow = 2,
                walkToColumn = 2,
            )
            // When
            eventBus.dispatch()
            // Then
            val previousTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 1)
            previousTileOccupantId.shouldBeNull()
            val newTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 2)
            newTileOccupantId shouldBe playerOneKnightBattleUnitId
        }

    @Test
    fun `should store the health potion when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            val healthPotionId = "health-potion"
            // When
            val healthPotion = lootableItemApi.searchLootableItemById(healthPotionId)
            // Then
            healthPotion.shouldNotBeNull()
            healthPotion.lootType.type shouldBe CONSUMABLE
            healthPotion.lootType.consumable?.effectIds shouldBe setOf("health-potion-heal")
        }

    @Test
    fun `should store the mana potion when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            val manaPotionId = "mana-potion"
            // When
            val manaPotion = lootableItemApi.searchLootableItemById(manaPotionId)
            // Then
            manaPotion.shouldNotBeNull()
            manaPotion.lootType.type shouldBe CONSUMABLE
            manaPotion.lootType.consumable?.effectIds shouldBe setOf("mana-potion-restore")
        }

    @Test
    fun `should store the hammer when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            val hammerId = "hammer"
            // When
            val hammer = lootableItemApi.searchLootableItemById(hammerId)
            // Then
            hammer.shouldNotBeNull()
            hammer.lootType.type shouldBe EQUIPPABLE
            hammer.lootType.equippable shouldBe EquippableDto(effectIds = setOf("maximum-health-20", "maximum-mana-20"), equipmentZone = WEAPON)
        }

    @Test
    fun `should store the tier 1 loot table when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            val tierOneLootTableId = "tier-1"
            // When
            val tierOneLootTable = lootTableApi.searchLootTableById(tierOneLootTableId)
            // Then
            tierOneLootTable.shouldNotBeNull()
            tierOneLootTable.entries.shouldContainExactly(
                EntryDto.lootableItem(lootableItemId = "health-potion", weight = 10),
                EntryDto.lootableItem(lootableItemId = "mana-potion", weight = 10),
                EntryDto.nothing(weight = 5),
            )
        }

    @Test
    fun `should store the tier 2 loot table when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            val tierTwoLootTableId = "tier-2"
            // When
            val tierTwoLootTable = lootTableApi.searchLootTableById(tierTwoLootTableId)
            // Then
            tierTwoLootTable.shouldNotBeNull()
            tierTwoLootTable.entries.shouldContainExactly(
                EntryDto.lootableItem(lootableItemId = "health-potion", weight = 10),
                EntryDto.lootableItem(lootableItemId = "mana-potion", weight = 10),
                EntryDto.lootableItem(lootableItemId = "hammer", weight = 3),
                EntryDto.nothing(weight = 5),
            )
        }
}
