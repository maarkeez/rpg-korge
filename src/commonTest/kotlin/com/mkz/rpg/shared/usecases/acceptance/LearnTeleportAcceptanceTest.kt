package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class LearnTeleportAcceptanceTest {
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
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, unitApi, lootableItemApi, partyApi, eventBus)
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleUnitApi, playerApi, battleApi, partyApi, npcApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val vikingBattleUnitId = "viking-1"
    private val vikingRow = 6
    private val vikingColumn = 7

    // The party explores with a viking of the human player, as if recruited, which only knows sword.
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
        battleUnitApi.deployBattleUnit(
            battleUnitId = vikingBattleUnitId,
            unitId = "viking",
            playerId = "player-one",
            deployAtRow = vikingRow,
            deployAtColumn = vikingColumn,
        )
        eventBus.dispatch()
    }

    @Test
    fun `should teach the viking teleport for good when the viking consumes a looted teleport essence`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle() == null)
            require(
                battleUnitApi
                    .searchBattleUnitById(vikingBattleUnitId)!!
                    .abilityCooldowns.keys
                    .toList() == listOf("sword"),
            )
            lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "teleport-essence", row = vikingRow, column = vikingColumn)
            inventoryApi.storeLootedItem(battleUnitId = vikingBattleUnitId, lootableItemId = "teleport-essence", row = vikingRow, column = vikingColumn)
            // When
            inventoryApi.consumeItem(battleUnitId = vikingBattleUnitId, lootableItemId = "teleport-essence")
            eventBus.dispatch()
            // Then
            val viking = battleUnitApi.searchBattleUnitById(vikingBattleUnitId)!!
            // Teleport is at the end of the ability bar, ready to be cast.
            viking.abilityCooldowns.toList().shouldContainExactly("sword" to 0, "teleport" to 0)
            inventoryApi.searchInventoryByBattleUnitId(vikingBattleUnitId)!!.storedItems.shouldBeEmpty()
        }
}
