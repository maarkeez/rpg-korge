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
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
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
    private val battleApi = BattleApi(eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
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
        lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "hammer", row = 6, column = 7)
        inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer", row = 6, column = 7)
        eventBus.dispatch()
    }

    private fun applyFromItem(
        lootableItemId: String,
        effectId: String,
    ) = battleUnitApi.applyEffect(
        EffectApplicationDto(
            source = SourceDto.item(lootableItemId),
            recipient = RecipientDto.battleUnit(playerOneKnightBattleUnitId),
            effectId = effectId,
            destination = null,
        ),
    )

    @Test
    fun `should raise the knight maximum health and mana without changing the remaining ones when the knight equips the hammer`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerId == playerOneId)
            // When
            inventoryApi.equipItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer")
            eventBus.dispatch()
            // Then
            val knight = battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!
            knight.maximumHealth shouldBe 120
            knight.maximumMana shouldBe 50
            knight.remainingHealth shouldBe 100
            knight.remainingMana shouldBe 30
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
            require(healedKnight.remainingHealth == 120 && healedKnight.remainingMana == 50)
            // When
            inventoryApi.unequipItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "hammer")
            eventBus.dispatch()
            // Then
            val knight = battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!
            knight.maximumHealth shouldBe 100
            knight.maximumMana shouldBe 30
            knight.remainingHealth shouldBe 100
            knight.remainingMana shouldBe 30
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.equippedItems shouldBe emptyMap()
        }
}
