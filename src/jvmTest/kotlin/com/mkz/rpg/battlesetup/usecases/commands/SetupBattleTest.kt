package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent
import org.junit.jupiter.api.Test

class SetupBattleTest {
    private val eventBus = FakeEventBus()
    private val setupBattle = SetupBattle(eventBus)

    @Test
    fun `should request each subdomain initialisation after the subdomains it refers to when the battle is set up`() {
        // Given
        // When
        setupBattle()
        // Then
        assertThat(eventBus).hasPublishedEvents(
            TerrainEvent.RequestInitialiseTerrains,
            EffectEvent.RequestInitialiseEffects,
            AbilityEvent.RequestInitialiseAbilities,
            UnitEvent.RequestInitialiseUnits,
            PlayerEvent.RequestInitialisePlayers,
            BattlefieldEvent.RequestInitialiseBattlefield,
            BattleUnitEvent.RequestInitialiseBattleUnits,
            BattleEvent.RequestInitialiseBattle,
        )
    }
}
