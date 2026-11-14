package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.tessavar.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.unit.domain.UnitMother.unit
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
            battleUnit(id = "knight-1", unit = unit(baseMaximumHealth = 100).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = increaseMaximumHealthEffect(maximumHealth = 20).toDto())
                .pullEvents()
                .second,
        )
        eventBus.publish(RequestRemoveEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"))
        // When
        eventBus.dispatch()
        // Then
        val battleUnit = battleUnitRepository.searchById("knight-1")?.toDto()
        battleUnit?.maximumHealth shouldBe 100
        battleUnit?.equippedEffects shouldBe emptyMap()
    }
}
