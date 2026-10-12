package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConfirmCastTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val confirmCast =
        ConfirmCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast the ability and keep the caster selected when the hud is previewing the ability cast`() {
        // Given
        val castGroup = castGroup(tile(1, 1))
        battlefieldHudRepository.create(
            displayAbilityCastPreview(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = setOf(tile(0, 1)),
                castGroup = castGroup,
            ),
        )
        // When
        confirmCast()
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = setOf(tile(0, 1)))
        // The caster movement range is displayed once the cast has been resolved.
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestCastAbility(
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup =
                    castGroup.tiles
                        .map { tile -> Battlefield.Dto.PositionDto(row = tile.row, column = tile.column) },
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
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { confirmCast() }
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
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { confirmCast() }
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
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { confirmCast() }
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        shouldThrow<BattlefieldHudError.BattlefieldHudNotFound> { confirmCast() }
    }
}
