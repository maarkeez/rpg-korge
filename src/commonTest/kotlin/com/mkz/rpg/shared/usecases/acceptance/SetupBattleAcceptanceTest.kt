package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SetupBattleAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

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
        battleSetupApi.setupBattle()
        eventBus.dispatch()
        // The setup starts exploring: the battle starts as if the players engaged each other.
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, "player-one", "player-two")
        eventBus.dispatch()
    }

    @Test
    fun `should occupy adjacent tile when battle unit was moved`() =
        scenarioTest(::setUp) {
            // Given
            val currentPosition = battlefieldApi.searchPosition(playerOneKnightBattleUnitId)!!
            require(currentPosition.row == 2 && currentPosition.column == 1)
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            battleUnitApi.moveBattleUnit(
                battleUnitId = playerOneKnightBattleUnitId,
                moveToRow = 2,
                moveToColumn = 2,
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
    fun `should store the health potion when the battle is set up`() =
        scenarioTest(::setUp) {
            // Given
            val healthPotionId = "health-potion"
            // When
            val healthPotion = lootableItemApi.searchLootableItemById(healthPotionId)
            // Then
            healthPotion.shouldNotBeNull()
            healthPotion.name shouldBe "Health Potion"
            healthPotion.lootType.type shouldBe CONSUMABLE
            healthPotion.lootType.consumable?.effectIds shouldBe setOf("health-potion-heal")
        }

    @Test
    fun `should store the mana potion when the battle is set up`() =
        scenarioTest(::setUp) {
            // Given
            val manaPotionId = "mana-potion"
            // When
            val manaPotion = lootableItemApi.searchLootableItemById(manaPotionId)
            // Then
            manaPotion.shouldNotBeNull()
            manaPotion.name shouldBe "Mana Potion"
            manaPotion.lootType.type shouldBe CONSUMABLE
            manaPotion.lootType.consumable?.effectIds shouldBe setOf("mana-potion-restore")
        }

    @Test
    fun `should store the hammer when the battle is set up`() =
        scenarioTest(::setUp) {
            // Given
            val hammerId = "hammer"
            // When
            val hammer = lootableItemApi.searchLootableItemById(hammerId)
            // Then
            hammer.shouldNotBeNull()
            hammer.name shouldBe "Hammer"
            hammer.lootType.type shouldBe EQUIPPABLE
            hammer.lootType.equippable shouldBe EquippableDto(effectIds = setOf("maximum-health-20", "maximum-mana-20"), equipmentZone = WEAPON)
        }

    @Test
    fun `should store the tier 1 loot table when the battle is set up`() =
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
    fun `should store the tier 2 loot table when the battle is set up`() =
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
