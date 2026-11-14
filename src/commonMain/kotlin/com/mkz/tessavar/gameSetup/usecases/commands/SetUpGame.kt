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
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.telemetry.domain.TelemetryEvent
import com.mkz.tessavar.terrain.domain.TerrainEvent
import com.mkz.tessavar.unit.domain.UnitEvent

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
