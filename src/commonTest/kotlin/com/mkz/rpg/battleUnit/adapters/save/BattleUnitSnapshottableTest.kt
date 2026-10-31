package com.mkz.rpg.battleUnit.adapters.save

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.commands.RestoreBattleUnits
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsSnapshot
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattleUnitSnapshottableTest {
    private val knightUnit = unit(id = "knight", healthPoints = 30, abilities = listOf("slash"))
    private val knight = knightUnit.toDto()
    private val human = player(id = "human").toDto()
    private val unitRepository = InMemoryUnitRepository().apply { create(knightUnit) }
    private val playerRepository = InMemoryPlayerRepository().apply { create(player(id = "human")) }
    private val abilityRepository = InMemoryAbilityRepository().apply { create(ability(id = "slash")) }
    private val effectRepository = InMemoryEffectRepository().apply { listOf(increaseMaximumHealthEffect(id = "iron-helmet"), increaseHealthEffect(id = "regeneration")).forEach(::create) }

    private fun snapshottable(repository: BattleUnitRepository) =
        BattleUnitSnapshottable(
            SearchBattleUnitsSnapshot(repository),
            RestoreBattleUnits(repository, SearchUnitById(unitRepository), SearchPlayerById(playerRepository), SearchAbilityById(abilityRepository), SearchEffectById(effectRepository)),
        )

    // A knight at 18 of 30 health points, wearing an iron helmet (+10 maximum health), regenerating for 2 more turns,
    // with slash cooling down after casting it, and able to know 4 abilities.
    private fun hurtKnight(): BattleUnit =
        battleUnit(id = "knight-1", unit = knight, player = human, maximumAbilities = 4, remainingHealthPoints = 18)
            .applyEquippedEffect("iron-helmet", effectRepository.searchById("iron-helmet")!!.toDto())
            .receiveOnTurnStartedEffect("regeneration", turnsLeft = 2)
            .castAbility(abilityId = "slash", abilityCooldown = 2, abilityCost = 0, castGroup = emptyList())
            .pullEvents()
            .second

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val knights = listOf(hurtKnight(), battleUnit(id = "knight-2", unit = knight, player = human))
        val snapshot = snapshottable(InMemoryBattleUnitRepository().apply { knights.forEach(::create) }).snapshot()
        val restoredRepository = InMemoryBattleUnitRepository()
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(knights)
    }

    @Test
    fun `Key should be battleUnit`() {
        // When
        val result = snapshottable(InMemoryBattleUnitRepository()).key
        // Then
        result shouldBe "battleUnit"
    }
}
