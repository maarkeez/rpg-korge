package com.mkz.tessavar.cpuWanderBrain.usecases.queries

import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class IsInSightTest {
    private val queries = InMemoryQueries()
    private val isInSight = queries.isInSight

    private fun givenBattlefield(
        leader: Pair<Int, Int>?,
        rat: Pair<Int, Int>,
    ) {
        val withRat = battlefield(rows = 40, columns = 24).occupy(row = rat.first, column = rat.second, occupantId = "rat-1")
        queries.battlefieldRepository.create(leader?.let { withRat.occupy(row = leader.first, column = leader.second, occupantId = "knight-1") } ?: withRat)
    }

    private fun givenParty() = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))

    @Test
    fun `should be in sight when it stands within 4 tiles of the leader on each axis`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 12, rat = 14 to 8)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeTrue()
    }

    @Test
    fun `should not be in sight when it stands 5 tiles away from the leader`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 12, rat = 10 to 17)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeFalse()
    }

    @Test
    fun `should shift the area in sight inside the battlefield near a border`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 1, rat = 10 to 7)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeTrue()
    }

    @Test
    fun `should shift the area in sight inside the battlefield near the last row`() {
        // Given
        givenParty()
        givenBattlefield(leader = 38 to 12, rat = 32 to 12)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeTrue()
    }

    @Test
    fun `should not be in sight without a party`() {
        // Given
        givenBattlefield(leader = 10 to 12, rat = 10 to 13)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeFalse()
    }

    @Test
    fun `should not be in sight when the leader is not on the battlefield`() {
        // Given
        givenParty()
        givenBattlefield(leader = null, rat = 10 to 13)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeFalse()
    }

    @Test
    fun `should not be in sight when it is not on the battlefield`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 12, rat = 10 to 13)
        // When / Then
        isInSight(battleUnitId = "rat-2").shouldBeFalse()
    }
}
