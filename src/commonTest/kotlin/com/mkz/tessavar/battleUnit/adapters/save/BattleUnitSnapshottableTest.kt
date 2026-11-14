package com.mkz.tessavar.battleUnit.adapters.save

import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.commands.LoadBattleUnitsSnapshot
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsSnapshot
import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitMother.unit
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattleUnitSnapshottableTest {
    private val knightUnit = unit(id = "knight", baseMaximumHealth = 30, startingAbilities = listOf("slash"))
    private val knight = knightUnit.toDto()
    private val human = player(id = "human").toDto()
    private val unitRepository = InMemoryUnitRepository().apply { create(knightUnit) }
    private val playerRepository = InMemoryPlayerRepository().apply { create(player(id = "human")) }
    private val abilityRepository = InMemoryAbilityRepository().apply { create(ability(id = "slash")) }
    private val effectRepository = InMemoryEffectRepository().apply { listOf(increaseMaximumHealthEffect(id = "iron-helmet"), healEffect(id = "regeneration")).forEach(::create) }

    private fun snapshottable(repository: BattleUnitRepository) =
        BattleUnitSnapshottable(
            SearchBattleUnitsSnapshot(repository),
            LoadBattleUnitsSnapshot(repository, SearchUnitById(unitRepository), SearchPlayerById(playerRepository), SearchAbilityById(abilityRepository), SearchEffectById(effectRepository)),
        )

    private fun hurtKnight(): BattleUnit =
        battleUnit(id = "knight-1", unit = knight, player = human, maximumAbilities = 4, remainingHealth = 18)
            .applyEquippedEffect("iron-helmet", effectRepository.searchById("iron-helmet")!!.toDto())
            .receiveOnPlayerTurnStartedEffect("regeneration", remainingDuration = 2)
            .castAbility(abilityId = "slash", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList())
            .pullEvents()
            .second

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val knights = listOf(hurtKnight(), battleUnit(id = "knight-2", unit = knight, player = human))
        val snapshot = snapshottable(InMemoryBattleUnitRepository().apply { knights.forEach(::create) }).snapshot()
        val loadedRepository = InMemoryBattleUnitRepository()
        // When
        snapshottable(loadedRepository).load(snapshot)
        // Then
        loadedRepository.searchAll().shouldContainExactlyInAnyOrder(knights)
    }

    @Test
    fun `Key should be battleUnit`() {
        // When
        val result = snapshottable(InMemoryBattleUnitRepository()).key
        // Then
        result shouldBe "battleUnit"
    }
}
