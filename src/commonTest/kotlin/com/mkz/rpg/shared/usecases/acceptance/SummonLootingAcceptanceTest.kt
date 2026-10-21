package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.services.FakeLootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootTable.usecases.services.FakeLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/** Both players are human, so the test plays every turn. */
class SummonLootingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus, FakeLootTableLoader(listOf(lootTable(id = "enemy-loot", entries = listOf(nothingEntry())).toDto())))
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, FakeLootDropLoader(listOf(lootDrop(unitId = "enemy", lootTableIds = listOf("enemy-loot")).toDto())))
    private val battleApi = BattleApi(eventBus, playerApi)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus)
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)

    private val humanPlayerId = "human"
    private val enemyPlayerId = "enemy"
    private val knightId = "knight-1"
    private val potionTile = LootDrop.Dto.PositionDto(row = 1, column = 4)

    // The knight, at (2,2), explores and summons a bee at (1,2), and a health potion lies at (1,4). The enemy stands far away, at (7,7).
    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(enemyPlayerId, "Enemy", HUMAN)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        effectApi.init()
        effectApi.initialiseEffects()
        lootableItemApi.init()
        lootableItemApi.initialiseLootableItems()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        effectApi.requestEffectCreation(EffectMother.deployBattleUnitEffect(id = "deploy-summon", unitId = "summon").toDto())
        abilityApi.requestAbilityCreation(
            AbilityMother
                .ability(
                    id = "summon",
                    cost = 0,
                    cooldown = 0,
                    effectSpecs = listOf(AbilityMother.effectSpec(effectId = "deploy-summon", target = TargetExpressionDto.Type.SELECTED_TILE)),
                    targeting = TargetingDto.VACANT_TILE_ADJACENT_TO_SELF,
                    turnsDuration = 5,
                ).toDto(),
        )
        unitApi.requestUnitCreation(UnitMother.unit(id = "knight", healthPoints = 20, manaPoints = 10, abilities = listOf("summon"), movementRange = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "summon", healthPoints = 20, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "enemy", healthPoints = 20, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto())
        lootTableApi.initialiseLootTables()
        lootDropApi.initialiseLootDrops()

        battleUnitApi.deployBattleUnit(battleUnitId = knightId, unitId = "knight", playerId = humanPlayerId, deployAtRow = 2, deployAtColumn = 2)
        battleUnitApi.deployBattleUnit(battleUnitId = "enemy-1", unitId = "enemy", playerId = enemyPlayerId, deployAtRow = 7, deployAtColumn = 7)
        eventBus.dispatch()
        partyApi.startExploration()
        eventBus.dispatch()
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, enemyPlayerId)
        eventBus.dispatch()
        lootDropApi.placeDroppedItem(unitId = "enemy", lootableItemId = "health-potion", row = potionTile.row, column = potionTile.column)
        eventBus.dispatch()
    }

    // Summons the bee at (1,2), and returns its battle unit.
    private fun summonBee(): String {
        val castGroup = battleUnitApi.whereCanCast(knightId, "summon").first { castGroup -> castGroup.positions.any { tile -> tile.row == 1 && tile.column == 2 } }
        battleUnitApi.castAbility(battleUnitId = knightId, abilityId = "summon", castGroup = castGroup)
        eventBus.dispatch()
        return battlefieldApi.searchOccupant(row = 1, column = 2).shouldNotBeNull()
    }

    // The bee walks from (1,2) to (1,5), over the potion.
    private fun walkBeeOverThePotion(beeId: String) {
        battleUnitApi.moveBattleUnit(battleUnitId = beeId, moveToRow = 1, moveToColumn = 5)
        eventBus.dispatch()
        require(battlefieldApi.searchPosition(beeId) == PositionDto(row = 1, column = 5))
    }

    @Test
    fun `should leave the item on its tile when a summoned battle unit walks over it`() =
        scenarioTest(::setUp) {
            // Given
            val beeId = summonBee()
            // When
            walkBeeOverThePotion(beeId)
            // Then
            lootDropApi.searchDroppedItems()[potionTile].shouldNotBeNull().shouldContainExactly("health-potion")
            inventoryApi.searchInventoryByBattleUnitId(beeId).shouldBeNull()
        }

    @Test
    fun `should let a battle unit with an inventory loot the item a summoned battle unit walked over`() =
        scenarioTest(::setUp) {
            // Given
            walkBeeOverThePotion(summonBee())
            // When
            battleUnitApi.moveBattleUnit(battleUnitId = knightId, moveToRow = potionTile.row, moveToColumn = potionTile.column)
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(knightId)!!.storedItems shouldBe listOf("health-potion")
            lootDropApi.searchDroppedItems() shouldNotContainKey potionTile
        }
}
