package com.mkz.rpg.gameSetup.usecases.commands

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
import com.mkz.rpg.quest.domain.QuestEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent
import kotlin.test.Test

class SetUpGameTest {
    private val eventBus = FakeEventBus()
    private val setUpGame = SetUpGame(eventBus)

    @Test
    fun `should request each subdomain initialisation after the subdomains it refers to, then exploring, then the main quest, when the game is set up`() {
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
            QuestEvent.RequestStartMainQuest,
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
