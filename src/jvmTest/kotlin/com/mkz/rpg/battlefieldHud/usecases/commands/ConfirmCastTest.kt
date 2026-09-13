package com.mkz.rpg.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ConfirmCast
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ConfirmCastTest {
    private val castAbility: CastAbility = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val confirmCast =
        ConfirmCast(
            castAbility = castAbility,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast the ability and go idle when the hud is previewing the ability cast`() {
        // Given
        val castGroup = castGroup(tile(1, 1))
        battlefieldHudRepository.create(
            displayAbilityCastPreview(
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
            ),
        )
        // When
        confirmCast()
        // Then
        verify(castAbility).invoke(
            battleUnitId = "battle-unit-1",
            abilityId = "ability-1",
            castGroup =
                WhereCanCast.CastGroup(
                    positions =
                        castGroup.tiles
                            .map { tile -> WhereCanCast.PositionDto(row = tile.row, column = tile.column) },
                ),
        )
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(),
        )
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastRange(),
        )
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
