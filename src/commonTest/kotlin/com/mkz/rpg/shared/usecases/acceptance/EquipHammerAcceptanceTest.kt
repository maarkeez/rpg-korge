package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EquipHammerAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
    private val npcApi = NpcApi(battlefieldApi, unitApi, lootableItemApi, eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, npcApi, eventBus)
    private val inventoryApi = InventoryApi(lootableItemApi, unitApi, battleUnitApi, playerApi, battleApi, partyApi, npcApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "player-1-unit-1"

    // The knight of the human player starts the battle with 100 health points and 30 mana points, and has looted the hammer.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        abilityApi.init()
        unitApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        battleSetupApi.setupBattle()
        eventBus.dispatch()
        // The setup starts exploring: the battle starts as if the players engaged each other.
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, "player-one", "player-two")
        eventBus.dispatch()
        battlefieldApi.placeDroppedItem(lootableItemId = "hammer", row = 6, column = 7)
        inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer", row = 6, column = 7)
        eventBus.dispatch()
    }

    private fun applyFromItem(
        lootableItemId: String,
        effectId: String,
    ) = battleUnitApi.applyEffect(
        EffectApplicationDto(
            source = ApplicationSourceDto.item(lootableItemId),
            target = EffectTargetDto.Unit(playerOneKnightBattleUnitId),
            effectId = effectId,
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
