package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnTurnStartedEffectApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class BattlefieldPresenterTest {
    private val battlefieldView = mock<BattlefieldView>()
    private val battleUnitInfoView = mock<BattleUnitInfoView>()
    private val attackPreviewView = mock<AttackPreviewView>()
    private val battleHudView = mock<BattleHudView>()
    private val searchOccupant = mock<SearchOccupant>()
    private val battlefieldApi = mock<BattlefieldApi>().also { whenever(it.searchOccupant).thenReturn(searchOccupant) }
    private val battleUnitApi = mock<BattleUnitApi>()
    private val playerApi = mock<PlayerApi>()
    private val unitApi = mock<UnitApi>()
    private val abilityApi = mock<AbilityApi>()
    private val battleApi = mock<BattleApi>()
    private val eventBus = InMemoryEventBus()
    private val timeline = battleSceneTimeline()
    private val battlefieldPresenter =
        BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = battleUnitInfoView,
            attackPreviewView = attackPreviewView,
            playerCallToActionView = mock(),
            battleHudView = battleHudView,
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            playerApi = playerApi,
            unitApi = unitApi,
            abilityApi = abilityApi,
            battleApi = battleApi,
            timeline = timeline,
            eventBus = eventBus,
        ).also {
            whenever(battlefieldApi.searchBattlefield).thenReturn(mock<SearchBattlefield>())
            whenever(battlefieldApi.searchPosition).thenReturn(mock<SearchPosition>())
            whenever(battlefieldApi.searchTilesThatCanBeOccupied).thenReturn(mock<SearchTilesThatCanBeOccupied>())
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(mock<SearchBattleUnitById>())
            whenever(battleUnitApi.canMoveTo).thenReturn(mock<CanMoveTo>())
            whenever(battleUnitApi.moveBattleUnit).thenReturn(mock<MoveBattleUnit>())
            whenever(battleUnitApi.canCastAbility).thenReturn(mock<CanCastAbility>())
            whenever(battleUnitApi.whereCanCast).thenReturn(mock<WhereCanCast>())
            whenever(battleUnitApi.castAbility).thenReturn(mock<CastAbility>())
            whenever(playerApi.searchPlayerById).thenReturn(mock<SearchPlayerById>())
            whenever(unitApi.searchUnitById).thenReturn(mock<SearchUnitById>())
            whenever(battleApi.searchBattle).thenReturn(mock<SearchBattle>())
            whenever(battleApi.finishPlayerTurn).thenReturn(mock<FinishPlayerTurn>())
        }

    @Nested
    inner class DisplayBattlefield {
        @Test
        fun `should display battlefield on view when battlefield is displayed`() {
            // Given
            val searchBattlefield = mock<SearchBattlefield>()
            val battlefield = battlefield(rows = 3, columns = 3).toDto()
            whenever(battlefieldApi.searchBattlefield).thenReturn(searchBattlefield)
            whenever(searchBattlefield()).thenReturn(battlefield)
            // When
            battlefieldPresenter.displayBattlefield()
            // Then
            verify(battlefieldView).displayBattlefield(battlefield)
        }

        @Test
        fun `should display battlefield on view when battlefield is created`() {
            // Given
            val searchBattlefield = mock<SearchBattlefield>()
            val battlefield = battlefield(rows = 3, columns = 3).toDto()
            whenever(battlefieldApi.searchBattlefield).thenReturn(searchBattlefield)
            whenever(searchBattlefield()).thenReturn(battlefield)
            eventBus.publish(BattlefieldCreated)
            // When
            eventBus.dispatch()
            // Then
            verify(battlefieldView).displayBattlefield(battlefield)
        }
    }

    @Nested
    inner class DisplayUnit {
        @Test
        fun `should display knight battle unit on view when knight battle unit is displayed`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            val battleUnitId = "battle-unit-1"
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById(battleUnitId)).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
            // When
            battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
            // Then
            verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "knight")
        }

        @Test
        fun `should display rat battle unit on view when rat battle unit is displayed`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            val battleUnitId = "battle-unit-1"
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById(battleUnitId)).thenReturn(battleUnit(unit = unit(id = "rat").toDto()).toDto())
            // When
            battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
            // Then
            verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "rat")
        }

        @Test
        fun `should not display battle unit on view when battle unit is not found`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            val battleUnitId = "battle-unit-1"
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById(battleUnitId)).thenReturn(null)
            // When
            battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
            // Then
            verify(battlefieldView, never()).displayBattleUnit(any(), any(), any())
        }
    }

    @Nested
    inner class RemoveUnit {
        @Test
        fun `should remove battle unit from view when battle unit is removed`() {
            // Given
            // When
            battlefieldPresenter.removeUnit(row = 1, column = 2)
            // Then
            verify(battlefieldView).removeBattleUnit(row = 1, column = 2)
        }
    }

    @Nested
    inner class DisplayUnitInOrder {
        @Test
        fun `should display the deployed battle unit once the previous steps have been played`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
            val previousStep = timeline.playLongStep()
            eventBus.publish(BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 1, column = 2))
            eventBus.dispatch()
            verify(battlefieldView, never()).displayBattleUnit(any(), any(), any())
            // When
            previousStep.complete(Unit)
            // Then
            verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "knight")
        }
    }

    @Nested
    inner class MoveUnit {
        @Test
        fun `should move the battle unit along its path on view when the battle unit is moved`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "rat").toDto()).toDto())
            val path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2))
            eventBus.publish(BattleUnitMoved(battleUnitId = "battle-unit-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, path = path))
            // When
            eventBus.dispatch()
            // Then
            verifyBlocking(battlefieldView) { moveBattleUnit(fromRow = 0, fromColumn = 0, path = path, unitId = "rat") }
        }

        @Test
        fun `should not move the battle unit on view when the battle unit is not found`() {
            // Given
            val searchBattleUnitById = mock<SearchBattleUnitById>()
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(searchBattleUnitById("battle-unit-1")).thenReturn(null)
            val path = listOf(PositionDto(row = 0, column = 1))
            eventBus.publish(BattleUnitMoved(battleUnitId = "battle-unit-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1, path = path))
            // When
            eventBus.dispatch()
            // Then
            verifyBlocking(battlefieldView, never()) { moveBattleUnit(any(), any(), any(), any()) }
        }
    }

    @Nested
    inner class FlashUnit {
        private val searchBattleUnitById = mock<SearchBattleUnitById>()
        private val searchPosition = mock<SearchPosition>()

        private fun knightAt(position: PositionDto?) {
            whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(battlefieldApi.searchPosition).thenReturn(searchPosition)
            whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
            whenever(searchPosition("battle-unit-1")).thenReturn(position)
        }

        @Test
        fun `should flash the battle unit on view when it receives an effect`() {
            // Given
            knightAt(PositionDto(row = 3, column = 4))
            eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
            // When
            eventBus.dispatch()
            // Then
            verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
        }

        @Test
        fun `should flash the battle unit on view when an effect is applied at the start of its turn`() {
            // Given
            knightAt(PositionDto(row = 3, column = 4))
            eventBus.publish(OnTurnStartedEffectApplied(battleUnitId = "battle-unit-1", effectId = "venom-damage"))
            // When
            eventBus.dispatch()
            // Then
            verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
        }

        @Test
        fun `should not flash the battle unit on view when it is not on the battlefield`() {
            // Given
            knightAt(null)
            eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
            // When
            eventBus.dispatch()
            // Then
            verifyBlocking(battlefieldView, never()) { flashBattleUnit(any(), any(), any()) }
        }

        @Test
        fun `should flash the battle unit once the previous steps have been played`() {
            // Given
            knightAt(PositionDto(row = 3, column = 4))
            val previousStep = timeline.playLongStep()
            eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
            eventBus.dispatch()
            verifyBlocking(battlefieldView, never()) { flashBattleUnit(any(), any(), any()) }
            // When
            previousStep.complete(Unit)
            // Then
            verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
        }
    }

    @Nested
    inner class TileSelected {
        @Test
        fun `should process the selected tile when the timeline is idle`() {
            // Given
            // When
            battlefieldPresenter.tileSelected(row = 1, column = 2)
            // Then
            verify(searchOccupant).invoke(row = 1, column = 2)
        }

        @Test
        fun `should ignore the selected tile when the timeline is playing`() {
            // Given
            timeline.playLongStep()
            // When
            battlefieldPresenter.tileSelected(row = 1, column = 2)
            // Then
            verify(searchOccupant, never()).invoke(any(), any())
        }
    }
}
