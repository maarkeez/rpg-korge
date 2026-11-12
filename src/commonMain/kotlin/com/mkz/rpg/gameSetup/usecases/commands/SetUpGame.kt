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
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.telemetry.domain.TelemetryEvent
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent

class SetUpGame(
    private val eventBus: EventBus,
) {
    operator fun invoke(isLoadingASave: Boolean = false) {
        eventBus.publish(TerrainEvent.RequestInitialiseTerrains)
        eventBus.publish(EffectEvent.RequestInitialiseEffects)
        eventBus.publish(LootableItemEvent.RequestInitialiseLootableItems)
        eventBus.publish(LootTableEvent.RequestInitialiseLootTables)
        eventBus.publish(AbilityEvent.RequestInitialiseAbilities)
        eventBus.publish(UnitEvent.RequestInitialiseUnits)
        eventBus.publish(LootDropEvent.RequestInitialiseLootDrops)
        eventBus.publish(PlayerEvent.RequestInitialisePlayers)
        eventBus.publish(BattlefieldEvent.RequestInitialiseBattlefield)
        eventBus.publish(BuildingEvent.RequestPlaceBuildings)
        eventBus.publish(QuestEvent.RequestCreateQuests)
        if (isLoadingASave) return
        eventBus.publish(BattleUnitEvent.RequestInitialiseBattleUnits)
        eventBus.publish(NpcEvent.RequestPlaceNpcs)
        eventBus.publish(PartyEvent.RequestStartExploration)
        eventBus.publish(TelemetryEvent.RequestStartGame)
    }
}
