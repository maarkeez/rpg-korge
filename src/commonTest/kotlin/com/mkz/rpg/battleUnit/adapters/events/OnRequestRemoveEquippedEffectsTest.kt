package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRemoveEquippedEffectsTest {
    private val eventBus = InMemoryEventBus()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val onRequestRemoveEquippedEffects = OnRequestRemoveEquippedEffects(RemoveEquippedEffects(battleUnitRepository, eventBus), eventBus)

    @Test
    fun `should remove the item effects from the battle unit when the removal of the equipped effects is requested`() {
        // Given
        battleUnitRepository.create(
            battleUnit(id = "knight-1", unit = unit(healthPoints = 100).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = increaseMaximumHealthEffect(healthPoints = 20).toDto())
                .pullEvents()
                .second,
        )
        eventBus.publish(RequestRemoveEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"))
        // When
        eventBus.dispatch()
        // Then
        val battleUnit = battleUnitRepository.searchById("knight-1")?.toDto()
        battleUnit?.maximumHealthPoints shouldBe 100
        battleUnit?.equippedEffects shouldBe emptyMap()
    }
}
