package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class IsInSightTest {
    private val queries = InMemoryQueries()
    private val isInSight = queries.isInSight

    // On a battlefield of 40 rows and 24 columns, the party leader, knight-1, and rat-1 stand on the given tiles.
    private fun givenBattlefield(
        leader: Pair<Int, Int>?,
        rat: Pair<Int, Int>,
    ) {
        val withRat = battlefield(rows = 40, columns = 24).occupy(row = rat.first, column = rat.second, occupantId = "rat-1")
        queries.battlefieldRepository.create(leader?.let { withRat.occupy(row = leader.first, column = leader.second, occupantId = "knight-1") } ?: withRat)
    }

    private fun givenParty() = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))

    @Test
    fun `should be in sight when it stands within 5 tiles of the leader on each axis`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 12, rat = 15 to 7)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeTrue()
    }

    @Test
    fun `should not be in sight when it stands 6 tiles away from the leader`() {
        // Given
        givenParty()
        givenBattlefield(leader = 10 to 12, rat = 10 to 18)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeFalse()
    }

    @Test
    fun `should shift the area in sight inside the battlefield near a border`() {
        // Given: the camera does not scroll left of the first column, so it shows the columns 0 to 8, and 9 is 1 tile around.
        givenParty()
        givenBattlefield(leader = 10 to 1, rat = 10 to 9)
        // When / Then
        isInSight(battleUnitId = "rat-1").shouldBeTrue()
    }

    @Test
    fun `should shift the area in sight inside the battlefield near the last row`() {
        // Given: the camera does not scroll below the last row, so it shows the rows 31 to 39, and 30 is 1 tile around.
        givenParty()
        givenBattlefield(leader = 38 to 12, rat = 30 to 12)
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
        // When / Then: rat-2 is not deployed.
        isInSight(battleUnitId = "rat-2").shouldBeFalse()
    }
}
