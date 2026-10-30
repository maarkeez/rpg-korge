package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.building.domain.BuildingEvent
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootTable.domain.LootTableEvent
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent
import kotlin.test.Test

class SetupBattleTest {
    private val eventBus = FakeEventBus()
    private val setupBattle = SetupBattle(eventBus)

    @Test
    fun `should request each subdomain initialisation after the subdomains it refers to, then exploring, when the battle is set up`() {
        // Given
        // When
        setupBattle()
        // Then
        eventBus.shouldHavePublishedEvents(
            TerrainEvent.RequestInitialiseTerrains,
            EffectEvent.RequestInitialiseEffects,
            LootableItemEvent.RequestInitialiseLootableItems,
            LootTableEvent.RequestInitialiseLootTables,
            AbilityEvent.RequestInitialiseAbilities,
            UnitEvent.RequestInitialiseUnits,
            LootDropEvent.RequestInitialiseLootDrops,
            PlayerEvent.RequestInitialisePlayers,
            BattlefieldEvent.RequestInitialiseBattlefield,
            BuildingEvent.RequestInitialiseBuildings,
            BattleUnitEvent.RequestInitialiseBattleUnits,
            NpcEvent.RequestInitialiseNpcs,
            PartyEvent.RequestStartExploration,
        )
    }

    @Test
    fun `should stop after the buildings, which the saved game is restored on, when a saved game is loaded`() {
        // When
        setupBattle(isLoadingASavedGame = true)
        // Then
        eventBus.shouldHavePublishedEvents(
            TerrainEvent.RequestInitialiseTerrains,
            EffectEvent.RequestInitialiseEffects,
            LootableItemEvent.RequestInitialiseLootableItems,
            LootTableEvent.RequestInitialiseLootTables,
            AbilityEvent.RequestInitialiseAbilities,
            UnitEvent.RequestInitialiseUnits,
            LootDropEvent.RequestInitialiseLootDrops,
            PlayerEvent.RequestInitialisePlayers,
            BattlefieldEvent.RequestInitialiseBattlefield,
            BuildingEvent.RequestInitialiseBuildings,
        )
    }
}
