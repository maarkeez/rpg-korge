package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.feedback.FeedbackTiming
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BattlefieldPresenterPlaybackTest {
    private val battlefieldView = mock<BattlefieldView>()
    private val playerCallToActionView = mock<PlayerCallToActionView>()
    private val battlefieldApi = mock<BattlefieldApi>()
    private val battleUnitApi = mock<BattleUnitApi>()
    private val battleApi = mock<BattleApi>()
    private val playerApi = mock<PlayerApi>()
    private val unitApi = mock<UnitApi>()
    private val eventBus = InMemoryEventBus()
    private val searchPosition = mock<SearchPosition>()
    private val searchBattleUnitById = mock<SearchBattleUnitById>()
    private val searchPlayerById = mock<SearchPlayerById>()
    private val searchUnitById = mock<SearchUnitById>()
    private val canBattlefieldTileBeOccupied = mock<CanBattlefieldTileBeOccupied>()

    private val knightUnit = unit(id = "knight", healthPoints = 100).toDto()
    private val humanPlayer = player(id = "human", type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
    private val cpuPlayer = player(id = "cpu", type = Player.Dto.PlayerTypeDto.CPU).toDto()

    private fun presenter(timing: FeedbackTiming): BattlefieldPresenter {
        whenever(battlefieldApi.searchPosition).thenReturn(searchPosition)
        whenever(battleApi.searchBattle).thenReturn(mock<SearchBattle>())
        whenever(battlefieldApi.canBattlefieldTileBeOccupied).thenReturn(canBattlefieldTileBeOccupied)
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(playerApi.searchPlayerById).thenReturn(searchPlayerById)
        whenever(unitApi.searchUnitById).thenReturn(searchUnitById)
        whenever(searchUnitById(knightUnit.id)).thenReturn(knightUnit)
        whenever(searchPlayerById(humanPlayer.id)).thenReturn(humanPlayer)
        whenever(searchPlayerById(cpuPlayer.id)).thenReturn(cpuPlayer)
        return BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = mock(),
            attackPreviewView = mock(),
            playerCallToActionView = playerCallToActionView,
            battleHudView = mock(),
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            playerApi = playerApi,
            unitApi = unitApi,
            abilityApi = mock<AbilityApi>(),
            battleApi = battleApi,
            eventBus = eventBus,
            feedbackTiming = timing,
        )
    }

    private fun givenBattleUnit(
        owner: Player.Dto,
        remainingHealthPoints: Int = 100,
        row: Int = 3,
        column: Int = 4,
    ): BattleUnit.Dto {
        val battleUnit = battleUnit(unit = knightUnit, player = owner).toDto().copy(remainingHealthPoints = remainingHealthPoints)
        whenever(searchBattleUnitById(battleUnit.id)).thenReturn(battleUnit)
        whenever(searchPosition(battleUnit.id)).thenReturn(Battlefield.Dto.PositionDto(row = row, column = column))
        return battleUnit
    }

    @Nested
    inner class Damage {
        @Test
        fun `should flash the unit and pop the damage taken when a battle unit is damaged`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 12, remainingHealthPoints = 88))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).playHitFlash(row = 3, column = 4, unitId = "knight")
            verify(battlefieldView).playAmountPop(row = 3, column = 4, amount = 12, heal = false)
        }

        @Test
        fun `should slash across the unit when a battle unit is damaged`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 12, remainingHealthPoints = 88))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).playHitSlash(row = 3, column = 4)
            verify(battlefieldView, never()).playHealSparkle(any(), any())
        }

        @Test
        fun `should pop the amount of the event and not a difference of repository state when a unit is hit twice`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer, remainingHealthPoints = 70)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 10, remainingHealthPoints = 90))
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 20, remainingHealthPoints = 70))
            eventBus.dispatch()
            presenter.updateFeedback(deltaMs = 10_000.0)
            // Then
            val order = inOrder(battlefieldView)
            order.verify(battlefieldView).playAmountPop(row = 3, column = 4, amount = 10, heal = false)
            order.verify(battlefieldView).playAmountPop(row = 3, column = 4, amount = 20, heal = false)
        }

        @Test
        fun `should not play any effect when the timing is instant`() {
            // Given
            val presenter = presenter(FeedbackTiming.Instant)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 12, remainingHealthPoints = 88))
            eventBus.dispatch()
            // Then
            verify(battlefieldView, never()).playHitFlash(any(), any(), any())
            verify(battlefieldView, never()).playAmountPop(any(), any(), any(), any())
        }
    }

    @Nested
    inner class Healing {
        @Test
        fun `should pop the healed amount without flashing the unit when a battle unit is healed`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val healedBattleUnit = givenBattleUnit(owner = humanPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitHealed(battleUnitId = healedBattleUnit.id, amount = 7, remainingHealthPoints = 100))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).playAmountPop(row = 3, column = 4, amount = 7, heal = true)
            verify(battlefieldView, never()).playHitFlash(any(), any(), any())
        }

        @Test
        fun `should raise heal sparkles over the unit without a slash when a battle unit is healed`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val healedBattleUnit = givenBattleUnit(owner = humanPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitHealed(battleUnitId = healedBattleUnit.id, amount = 7, remainingHealthPoints = 100))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).playHealSparkle(row = 3, column = 4)
            verify(battlefieldView, never()).playHitSlash(any(), any())
        }

        @Test
        fun `should not pop a number when the heal restored nothing`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val healedBattleUnit = givenBattleUnit(owner = humanPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitHealed(battleUnitId = healedBattleUnit.id, amount = 0, remainingHealthPoints = 100))
            eventBus.dispatch()
            // Then
            verify(battlefieldView, never()).playAmountPop(any(), any(), any(), any())
        }
    }

    @Nested
    inner class Defeat {
        @Test
        fun `should play a poof and remove the unit when a battle unit is defeated`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val defeatedBattleUnit = givenBattleUnit(owner = cpuPlayer, remainingHealthPoints = 0)
            // When
            eventBus.publish(
                BattleUnitEvent.BattleUnitDefeated(playerId = cpuPlayer.id, battleUnitId = defeatedBattleUnit.id, defeatedAtRow = 3, defeatedAtColumn = 4),
            )
            eventBus.dispatch()
            // Then
            verify(battlefieldView).removeBattleUnit(row = 3, column = 4)
            verify(battlefieldView).playPoof(row = 3, column = 4)
        }

        @Test
        fun `should send a spark to the neighbour when a defeated unit spreads an effect to it`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val defeatedBattleUnit = givenBattleUnit(owner = cpuPlayer, remainingHealthPoints = 0, row = 3, column = 4)
            val neighbour = givenBattleUnit(owner = cpuPlayer, row = 3, column = 5)
            eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = neighbour.id, row = 3, column = 5))
            eventBus.publish(
                BattleUnitEvent.BattleUnitDefeated(playerId = cpuPlayer.id, battleUnitId = defeatedBattleUnit.id, defeatedAtRow = 3, defeatedAtColumn = 4),
            )
            // When
            eventBus.publish(
                BattleUnitEvent.RequestApplyEffect(
                    Effect.Dto.EffectApplicationDto(
                        source =
                            Effect.Dto.EffectApplicationDto.ApplicationSourceDto
                                .battleUnit(defeatedBattleUnit.id),
                        target = Effect.Dto.EffectTargetDto.Unit(neighbour.id),
                        effectId = "venom-on-death",
                    ),
                ),
            )
            eventBus.dispatch()
            presenter.updateFeedback(deltaMs = 10_000.0)
            presenter.updateFeedback(deltaMs = 10_000.0)
            // Then
            verify(battlefieldView).playSpark(eq(3), eq(4), eq(3), eq(5), any())
        }
    }

    @Nested
    inner class Walking {
        @Test
        fun `should walk the unit tile by tile and draw it on the destination when it moves several tiles`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val movedBattleUnit = givenBattleUnit(owner = humanPlayer, row = 0, column = 3)
            whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(true)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitMoved(movedBattleUnit.id, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 3))
            eventBus.dispatch()
            repeat(5) { presenter.updateFeedback(deltaMs = 1_000.0) }
            // Then
            val order = inOrder(battlefieldView)
            order.verify(battlefieldView).removeBattleUnit(row = 0, column = 0)
            order.verify(battlefieldView).showWalker(eq("knight"), eq(0), eq(1), any())
            order.verify(battlefieldView).showWalker(eq("knight"), eq(0), eq(2), any())
            order.verify(battlefieldView).hideWalker(movedBattleUnit.id)
            order.verify(battlefieldView).displayKnightBattleUnit(row = 0, column = 3)
        }

        @Test
        fun `should turn the unit to look the way it walked when it moves to the left`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val movedBattleUnit = givenBattleUnit(owner = humanPlayer, row = 0, column = 0)
            whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(true)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitMoved(movedBattleUnit.id, fromRow = 0, fromColumn = 3, toRow = 0, toColumn = 0))
            eventBus.dispatch()
            repeat(5) { presenter.updateFeedback(deltaMs = 1_000.0) }
            // Then
            verify(battlefieldView, atLeastOnce()).turnWalker(movedBattleUnit.id, towardsRight = false)
            verify(battlefieldView).turnUnit(row = 0, column = 0, towardsRight = false)
        }

        @Test
        fun `should blink at both tiles and not walk when the unit teleports`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val teleportedBattleUnit = givenBattleUnit(owner = humanPlayer, row = 5, column = 5)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitTeleported(teleportedBattleUnit.id))
            eventBus.publish(BattleUnitEvent.BattleUnitMoved(teleportedBattleUnit.id, fromRow = 0, fromColumn = 0, toRow = 5, toColumn = 5))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).playHitFlash(row = 0, column = 0, unitId = "knight")
            verify(battlefieldView).playHitFlash(row = 5, column = 5, unitId = "knight")
            verify(battlefieldView, never()).showWalker(any(), any(), any(), any())
        }

        @Test
        fun `should follow the unit with the camera when a unit of the cpu moves`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val cpuBattleUnit = givenBattleUnit(owner = cpuPlayer, row = 0, column = 1)
            whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(true)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitMoved(cpuBattleUnit.id, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1))
            eventBus.dispatch()
            // Then
            verify(battlefieldView).centerOn(row = 0, column = 0)
        }

        @Test
        fun `should not move the camera when a unit of the player moves`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val humanBattleUnit = givenBattleUnit(owner = humanPlayer, row = 0, column = 1)
            whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(true)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitMoved(humanBattleUnit.id, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1))
            eventBus.dispatch()
            // Then
            verify(battlefieldView, never()).centerOn(any(), any())
        }
    }

    @Nested
    inner class Playback {
        @Test
        fun `should lock the call to action while playback runs and unlock it when it ends`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer)
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 12, remainingHealthPoints = 88))
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 5, remainingHealthPoints = 83))
            // When
            eventBus.dispatch()
            presenter.updateFeedback(deltaMs = 10_000.0)
            presenter.updateFeedback(deltaMs = 10_000.0)
            // Then
            val order = inOrder(playerCallToActionView)
            order.verify(playerCallToActionView).setPlaybackLocked(true)
            order.verify(playerCallToActionView).setPlaybackLocked(false)
        }

        @Test
        fun `should advance the view effects when playback is updated`() {
            // Given
            val presenter = presenter(FeedbackTiming.Standard)
            // When
            presenter.updateFeedback(deltaMs = 16.0)
            // Then
            verify(battlefieldView).advanceFx(16.0)
        }

        @Test
        fun `should not lock the call to action when the timing is instant`() {
            // Given
            val presenter = presenter(FeedbackTiming.Instant)
            val damagedBattleUnit = givenBattleUnit(owner = cpuPlayer)
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 12, remainingHealthPoints = 88))
            eventBus.dispatch()
            // Then
            verify(playerCallToActionView, never()).setPlaybackLocked(anyOrNull())
        }
    }
}
