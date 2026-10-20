package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MarkBattleUnitMovedTest {
    private val partyRepository = InMemoryPartyRepository()
    private val markBattleUnitMoved = MarkBattleUnitMoved(partyRepository)

    @Test
    fun `should remember the battle unit moved during the current second when it moves while exploring`() {
        // Given
        partyRepository.save(party())
        // When
        markBattleUnitMoved("knight-1")
        // Then
        partyRepository.search()!!.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-2")
    }

    @Test
    fun `should do nothing when nobody explores`() {
        // When
        markBattleUnitMoved("knight-1")
        // Then
        partyRepository.search().shouldBeNull()
    }
}
