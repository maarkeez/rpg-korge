package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
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
import org.mockito.kotlin.whenever

class BattlefieldPresenterTest {
    private val battlefieldView = mock<BattlefieldView>()
    private val battleUnitInfoView = mock<BattleUnitInfoView>()
    private val attackPreviewView = mock<AttackPreviewView>()
    private val battleHudView = mock<BattleHudView>()
    private val battlefieldApi = mock<BattlefieldApi>()
    private val battleUnitApi = mock<BattleUnitApi>()
    private val playerApi = mock<PlayerApi>()
    private val unitApi = mock<UnitApi>()
    private val abilityApi = mock<AbilityApi>()
    private val battleApi = mock<BattleApi>()
    private val eventBus = InMemoryEventBus()
    private val battlefieldPresenter =
        BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = battleUnitInfoView,
            attackPreviewView = attackPreviewView,
            battleHudView = battleHudView,
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            playerApi = playerApi,
            unitApi = unitApi,
            abilityApi = abilityApi,
            battleApi = battleApi,
            eventBus = eventBus,
        ).also {
            whenever(battlefieldApi.searchBattlefield).thenReturn(mock<SearchBattlefield>())
            whenever(battlefieldApi.searchOccupant).thenReturn(mock<SearchOccupant>())
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
            verify(battlefieldView).displayKnightBattleUnit(row = 1, column = 2)
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
            verify(battlefieldView).displayRatBattleUnit(row = 1, column = 2)
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
            verify(battlefieldView, never()).displayKnightBattleUnit(any(), any())
            verify(battlefieldView, never()).displayRatBattleUnit(any(), any())
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
}
