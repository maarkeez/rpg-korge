package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
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
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class EquipHammerAcceptanceTest {
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
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "hero"

    // The knight of the human player starts the battle with 100 health points and 30 mana points, and has looted the hammer.
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
        lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "hammer", row = 6, column = 7)
        inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer", row = 6, column = 7)
        eventBus.dispatch()
    }

    private fun applyFromItem(
        lootableItemId: String,
        effectId: String,
    ) = battleUnitApi.applyEffect(
        EffectApplicationDto(
            source = ApplicationSourceDto.item(lootableItemId),
            target = EffectTargetDto.battleUnit(playerOneKnightBattleUnitId),
            effectId = effectId,
            destination = null,
        ),
    )

    @Test
    fun `should raise the knight maximum health and mana without changing the remaining ones when the knight equips the hammer`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            // When
            inventoryApi.equipItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer")
            eventBus.dispatch()
            // Then
            val knight = battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!
            knight.maximumHealthPoints shouldBe 120
            knight.maximumManaPoints shouldBe 50
            knight.remainingHealthPoints shouldBe 100
            knight.remainingManaPoints shouldBe 30
            knight.equippedEffects shouldBe mapOf("hammer" to listOf("maximum-health-20", "maximum-mana-20"))
            val inventory = inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!
            inventory.equippedItems shouldBe mapOf(WEAPON to "hammer")
            inventory.storedItems.shouldContainExactly("hammer")
        }

    @Test
    fun `should lower the knight health and mana to the unit maximums when the knight unequips the hammer after healing and restoring mana above them`() =
        scenarioTest(::setUp) {
            // Given
            inventoryApi.equipItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer")
            eventBus.dispatch()
            repeat(2) { applyFromItem(lootableItemId = "health-potion", effectId = "health-potion-heal") }
            repeat(2) { applyFromItem(lootableItemId = "mana-potion", effectId = "mana-potion-restore") }
            eventBus.dispatch()
            val healedKnight = battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!
            require(healedKnight.remainingHealthPoints == 120 && healedKnight.remainingManaPoints == 50)
            // When
            inventoryApi.unequipItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer")
            eventBus.dispatch()
            // Then
            val knight = battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!
            knight.maximumHealthPoints shouldBe 100
            knight.maximumManaPoints shouldBe 30
            knight.remainingHealthPoints shouldBe 100
            knight.remainingManaPoints shouldBe 30
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.equippedItems shouldBe emptyMap()
        }
}
