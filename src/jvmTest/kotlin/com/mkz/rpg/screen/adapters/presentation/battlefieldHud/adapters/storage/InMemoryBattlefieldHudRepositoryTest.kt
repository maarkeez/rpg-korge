package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.idle
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryBattlefieldHudRepositoryTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the battlefield hud when the battlefield hud is created`() {
            // Given
            val createdBattlefieldHud = idle()
            // When
            battlefieldHudRepository.create(createdBattlefieldHud)
            // Then
            val storedBattlefieldHud = battlefieldHudRepository.search()
            storedBattlefieldHud shouldBe createdBattlefieldHud
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should replace the stored battlefield hud when the battlefield hud is updated`() {
            // Given
            val storedBattlefieldHud = idle()
            battlefieldHudRepository.create(storedBattlefieldHud)
            val updatedBattlefieldHud = displayMovementRange()
            // When
            battlefieldHudRepository.update(updatedBattlefieldHud)
            // Then
            val searchResult = battlefieldHudRepository.search()
            searchResult shouldBe updatedBattlefieldHud
        }
    }

    @Nested
    inner class Search {
        @Test
        fun `should return null when no battlefield hud is stored`() {
            // When
            val storedBattlefieldHud = battlefieldHudRepository.search()
            // Then
            storedBattlefieldHud.shouldBeNull()
        }
    }
}
