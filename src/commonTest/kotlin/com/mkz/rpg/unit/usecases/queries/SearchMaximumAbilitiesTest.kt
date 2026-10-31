package com.mkz.rpg.unit.usecases.queries

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchMaximumAbilitiesTest {
    private val searchMaximumAbilities = SearchMaximumAbilities()

    @Test
    fun `should return the maximum abilities of a unit`() {
        // When
        val maximumAbilities = searchMaximumAbilities()
        // Then
        maximumAbilities shouldBe 6
    }
}
