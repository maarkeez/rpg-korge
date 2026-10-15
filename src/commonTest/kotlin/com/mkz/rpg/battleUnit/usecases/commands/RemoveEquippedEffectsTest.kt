package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EquippedEffectsRemoved
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RemoveEquippedEffectsTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val removeEquippedEffects = RemoveEquippedEffects(battleUnitRepository, eventBus)

    @Test
    fun `should remove the item effects and publish the equipped effects removed event when the item is unequipped`() {
        // Given
        battleUnitRepository.create(
            battleUnit(id = "knight-1", unit = unit(healthPoints = 100).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = increaseMaximumHealthEffect(healthPoints = 20).toDto())
                .pullEvents()
                .second,
        )
        // When
        removeEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        battleUnitRepository.searchById("knight-1")?.toDto()?.maximumHealthPoints shouldBe 100
        eventBus.shouldHavePublishedEvents(EquippedEffectsRemoved(battleUnitId = "knight-1", lootableItemId = "hammer"))
    }

    @Test
    fun `should fail when the battle unit does not exist`() {
        // Given
        // When
        val removal = { removeEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<BattleUnitDoesNotExists> { removal() }
    }
}
