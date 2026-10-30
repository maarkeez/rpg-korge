package com.mkz.rpg.battleUnit.adapters.save

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class BattleUnitSnapshottableTest {
    private val knightUnit = unit(id = "knight", healthPoints = 30, abilities = listOf("slash"))
    private val knight = knightUnit.toDto()
    private val human = player(id = "human").toDto()
    private val unitRepository = InMemoryUnitRepository().apply { create(knightUnit) }
    private val playerRepository = InMemoryPlayerRepository().apply { create(player(id = "human")) }
    private val abilityRepository = InMemoryAbilityRepository().apply { create(ability(id = "slash")) }
    private val effectRepository = InMemoryEffectRepository().apply { listOf(increaseMaximumHealthEffect(id = "iron-helmet"), increaseHealthEffect(id = "regeneration")).forEach(::create) }
    private val battleUnitRepository = InMemoryBattleUnitRepository()

    private fun snapshottable(repository: InMemoryBattleUnitRepository = battleUnitRepository) =
        BattleUnitSnapshottable(repository, SearchUnitById(unitRepository), SearchPlayerById(playerRepository), SearchAbilityById(abilityRepository), SearchEffectById(effectRepository))

    // A knight at 18 of 30 health points, wearing an iron helmet (+10 maximum health), regenerating for 2 more turns,
    // and with slash cooling down after casting it.
    private fun hurtKnight(): BattleUnit =
        battleUnit(id = "knight-1", unit = knight, player = human, remainingHealthPoints = 18)
            .applyEquippedEffect("iron-helmet", effectRepository.searchById("iron-helmet")!!.toDto())
            .receiveOnTurnStartedEffect("regeneration", turnsLeft = 2)
            .castAbility(abilityId = "slash", abilityCooldown = 2, abilityCost = 0, castGroup = emptyList())
            .pullEvents()
            .second

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val knights = listOf(hurtKnight(), battleUnit(id = "knight-2", unit = knight, player = human))
        knights.forEach(battleUnitRepository::create)
        val snapshot = snapshottable().snapshot()
        val restoredRepository = InMemoryBattleUnitRepository().apply { create(battleUnit(id = "knight-3", unit = knight, player = human)) }
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(knights)
        restoredRepository.searchById("knight-1")!!.toDto().maximumHealthPoints shouldBe knight.healthPoints + 10
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        battleUnitRepository.create(hurtKnight())
        val restoredRepository = InMemoryBattleUnitRepository()
        // When
        snapshottable(restoredRepository).restore(snapshottable().snapshot())
        // Then
        restoredRepository.searchAll().flatMap { battleUnit -> battleUnit.pullEvents().first }.shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when a battle unit is of a unit the game no longer has`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "goblin-1", unit = unit(id = "goblin-archer").toDto(), player = human))
        // When
        val result = runCatching { snapshottable(InMemoryBattleUnitRepository()).restore(snapshottable().snapshot()) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "goblin-archer"
    }
}
