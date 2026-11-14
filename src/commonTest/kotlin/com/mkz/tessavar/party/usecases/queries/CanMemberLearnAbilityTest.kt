package com.mkz.tessavar.party.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

class CanMemberLearnAbilityTest {
    private val queries = InMemoryQueries()
    private val canMemberLearnAbility = CanMemberLearnAbility(queries.partyRepository, queries.canLearnAbility)

    @BeforeTest
    fun setUp() {
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("sword")).toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(startingAbilities = emptyList()).toDto()))
    }

    @Test
    fun `should allow learning an unknown ability when the battle unit is a party member`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // Then
        canMemberLearnAbility(battleUnitId = "knight-1", abilityId = "teleport").shouldBeTrue()
    }

    @Test
    fun `should not allow learning an ability the party member already knows`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // Then
        canMemberLearnAbility(battleUnitId = "knight-1", abilityId = "sword").shouldBeFalse()
    }

    @Test
    fun `should not allow learning when the battle unit is no party member`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // Then
        canMemberLearnAbility(battleUnitId = "rat-1", abilityId = "teleport").shouldBeFalse()
    }

    @Test
    fun `should not allow learning when there is no party`() {
        // Then
        canMemberLearnAbility(battleUnitId = "knight-1", abilityId = "teleport").shouldBeFalse()
    }
}
