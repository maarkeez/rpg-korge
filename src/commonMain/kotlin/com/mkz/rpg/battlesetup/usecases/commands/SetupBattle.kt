package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootTable.domain.LootTableEvent
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent

/**
 * Initialises each subdomain from its resources, then starts exploring. The order matters, as each subdomain
 * requires the ones it refers to (e.g.: abilities require their effects). The battle starts once the CPU engages.
 */
class SetupBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        eventBus.publish(TerrainEvent.RequestInitialiseTerrains)
        eventBus.publish(EffectEvent.RequestInitialiseEffects)
        eventBus.publish(LootableItemEvent.RequestInitialiseLootableItems)
        eventBus.publish(LootTableEvent.RequestInitialiseLootTables)
        eventBus.publish(AbilityEvent.RequestInitialiseAbilities)
        eventBus.publish(UnitEvent.RequestInitialiseUnits)
        eventBus.publish(LootDropEvent.RequestInitialiseLootDrops)
        eventBus.publish(PlayerEvent.RequestInitialisePlayers)
        eventBus.publish(BattlefieldEvent.RequestInitialiseBattlefield)
        eventBus.publish(BattleUnitEvent.RequestInitialiseBattleUnits)
        eventBus.publish(NpcEvent.RequestInitialiseNpcs)
        eventBus.publish(ExplorationEvent.RequestStartExploration)
    }
}
