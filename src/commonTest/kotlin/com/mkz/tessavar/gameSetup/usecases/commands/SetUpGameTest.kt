package com.mkz.tessavar.gameSetup.usecases.commands

import com.mkz.tessavar.ability.domain.AbilityEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.building.domain.BuildingEvent
import com.mkz.tessavar.effect.domain.EffectEvent
import com.mkz.tessavar.lootDrop.domain.LootDropEvent
import com.mkz.tessavar.lootTable.domain.LootTableEvent
import com.mkz.tessavar.lootableItem.domain.LootableItemEvent
import com.mkz.tessavar.npc.domain.NpcEvent
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.player.domain.PlayerEvent
import com.mkz.tessavar.quest.domain.QuestEvent
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.telemetry.domain.TelemetryEvent
import com.mkz.tessavar.terrain.domain.TerrainEvent
import com.mkz.tessavar.unit.domain.UnitEvent
import kotlin.test.Test

class SetUpGameTest {
    private val eventBus = FakeEventBus()
    private val setUpGame = SetUpGame(eventBus)

    @Test
    fun `should request each subdomain initialisation after the subdomains it refers to, then exploring and recording the game started, when the game is set up`() {
        // Given
        // When
        setUpGame()
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
            BuildingEvent.RequestPlaceBuildings,
            QuestEvent.RequestCreateQuests,
            BattleUnitEvent.RequestInitialiseBattleUnits,
            NpcEvent.RequestPlaceNpcs,
            PartyEvent.RequestStartExploration,
            TelemetryEvent.RequestStartGame,
        )
    }

    @Test
    fun `should stop after the quests, which the save is loaded on, when a save is loaded`() {
        // When
        setUpGame(isLoadingASave = true)
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
            BuildingEvent.RequestPlaceBuildings,
            QuestEvent.RequestCreateQuests,
        )
    }
}
