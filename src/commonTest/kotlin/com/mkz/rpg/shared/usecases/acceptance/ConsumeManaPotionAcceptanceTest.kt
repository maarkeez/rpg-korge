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
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class ConsumeManaPotionAcceptanceTest {
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

    // The knight of the human player starts the battle with 30 mana points, and spends 10 casting heal on itself.
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
        val castGroup = battleUnitApi.whereCanCast(playerOneKnightBattleUnitId, "heal").single()
        battleUnitApi.castAbility(battleUnitId = playerOneKnightBattleUnitId, abilityId = "heal", castGroup = castGroup)
        eventBus.dispatch()
    }

    @Test
    fun `should restore the knight mana when the knight consumes a looted mana potion`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            require(battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingManaPoints == 20)
            lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "mana-potion", row = 6, column = 7)
            inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "mana-potion", row = 6, column = 7)
            // When
            inventoryApi.consumeItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "mana-potion")
            eventBus.dispatch()
            // Then
            battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingManaPoints shouldBe 30
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems.shouldBeEmpty()
        }
}
