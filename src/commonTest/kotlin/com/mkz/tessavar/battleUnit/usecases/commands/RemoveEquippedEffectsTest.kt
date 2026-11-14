package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.EquippedEffectsRemoved
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.unit.domain.UnitMother.unit
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
            battleUnit(id = "knight-1", unit = unit(baseMaximumHealth = 100).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = increaseMaximumHealthEffect(maximumHealth = 20).toDto())
                .pullEvents()
                .second,
        )
        // When
        removeEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        battleUnitRepository.searchById("knight-1")?.toDto()?.maximumHealth shouldBe 100
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
