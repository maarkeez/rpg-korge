package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CancelCastTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val cancelCast =
        CancelCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should go back to the ability cast range when the hud is previewing the ability cast`() {
        // Given
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)), castGroup(tile(0, 1)))
        battlefieldHudRepository.create(
            displayAbilityCastPreview(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                castGroup = castGroup(tile(1, 1)),
            ),
        )
        // When
        cancelCast()
        // Then
        battlefieldHudRepository.search() shouldBe
            displayAbilityCastRange(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        // When
        // Then
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { cancelCast() }
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(),
        )
        // When
        // Then
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { cancelCast() }
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastRange(),
        )
        // When
        // Then
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { cancelCast() }
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        shouldThrow<BattlefieldHudError.BattlefieldHudNotFound> { cancelCast() }
    }
}
