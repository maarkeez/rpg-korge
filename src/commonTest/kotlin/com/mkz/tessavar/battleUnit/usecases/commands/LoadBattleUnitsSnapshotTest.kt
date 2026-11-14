package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
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
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class LoadBattleUnitsSnapshotTest {
    private val knightUnit = unit(id = "knight", baseMaximumHealth = 30, startingAbilities = listOf("slash"))
    private val knight = knightUnit.toDto()
    private val human = player(id = "human").toDto()
    private val effectRepository = InMemoryEffectRepository().apply { listOf(increaseMaximumHealthEffect(id = "iron-helmet"), healEffect(id = "regeneration")).forEach(::create) }
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val loadBattleUnitsSnapshot =
        LoadBattleUnitsSnapshot(
            battleUnitRepository,
            SearchUnitById(InMemoryUnitRepository().apply { create(knightUnit) }),
            SearchPlayerById(InMemoryPlayerRepository().apply { create(player(id = "human")) }),
            SearchAbilityById(InMemoryAbilityRepository().apply { create(ability(id = "slash")) }),
            SearchEffectById(effectRepository),
        )

    private fun hurtKnight(): BattleUnit =
        battleUnit(id = "knight-1", unit = knight, player = human, maximumAbilities = 4, remainingHealth = 18)
            .applyEquippedEffect("iron-helmet", effectRepository.searchById("iron-helmet")!!.toDto())
            .receiveOnPlayerTurnStartedEffect("regeneration", remainingDuration = 2)
            .castAbility(abilityId = "slash", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList())
            .pullEvents()
            .second

    @Test
    fun `should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val knights = listOf(hurtKnight(), battleUnit(id = "knight-2", unit = knight, player = human))
        battleUnitRepository.create(battleUnit(id = "knight-3", unit = knight, player = human))
        // When
        loadBattleUnitsSnapshot(knights.map(BattleUnit::toSnapshot))
        // Then
        battleUnitRepository.searchAll().shouldContainExactlyInAnyOrder(knights)
        battleUnitRepository.searchById("knight-1")!!.toDto().maximumHealth shouldBe knight.baseMaximumHealth + 10
    }

    @Test
    fun `should fail when a battle unit is of a unit the game no longer has`() {
        // Given
        val goblin = battleUnit(id = "goblin-1", unit = unit(id = "goblin-archer").toDto(), player = human)
        // When
        val result = runCatching { loadBattleUnitsSnapshot(listOf(goblin.toSnapshot())) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "goblin-archer"
    }
}
