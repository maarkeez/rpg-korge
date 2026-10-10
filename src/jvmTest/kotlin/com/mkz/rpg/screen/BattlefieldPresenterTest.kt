package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
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
    private val searchAbilityAvailability = mock<SearchAbilityAvailability>()
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
            playerCallToActionView = mock(),
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
            whenever(battleUnitApi.searchAbilityAvailability).thenReturn(searchAbilityAvailability)
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

    @Nested
    inner class UnitOverlay {
        @Test
        fun `should display overlay with remaining health fraction when a damaged battle unit is displayed`() {
            // Given
            val unit = unit(id = "rat", healthPoints = 20).toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.CPU).toDto()
            val damagedBattleUnit = battleUnit(unit = unit, player = player).toDto().copy(remainingHealthPoints = 5)
            givenBattleUnit(damagedBattleUnit, unit, player)
            // When
            battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = damagedBattleUnit.id)
            // Then
            val state = argumentCaptor<UnitOverlayState>()
            verify(battlefieldView).displayUnitOverlay(eq(1), eq(2), state.capture())
            assertThat(state.firstValue.hpFraction).isEqualTo(0.25)
            assertThat(state.firstValue.isEnemy).isTrue()
        }

        @Test
        fun `should refresh overlay at the current position when the battle unit is damaged`() {
            // Given
            val unit = unit(id = "knight", healthPoints = 100).toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
            val damagedBattleUnit = battleUnit(unit = unit, player = player).toDto().copy(remainingHealthPoints = 40)
            givenBattleUnit(damagedBattleUnit, unit, player)
            val searchPosition = mock<SearchPosition>()
            whenever(battlefieldApi.searchPosition).thenReturn(searchPosition)
            whenever(searchPosition(damagedBattleUnit.id)).thenReturn(Battlefield.Dto.PositionDto(row = 3, column = 4))
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitDamaged(battleUnitId = damagedBattleUnit.id, amount = 60, remainingHealthPoints = 40))
            eventBus.dispatch()
            // Then
            val state = argumentCaptor<UnitOverlayState>()
            verify(battlefieldView).displayUnitOverlay(eq(3), eq(4), state.capture())
            assertThat(state.firstValue.hpFraction).isEqualTo(0.4)
            assertThat(state.firstValue.isEnemy).isFalse()
        }

        @Test
        fun `should refresh overlay with the new health when the battle unit is healed`() {
            // Given
            val unit = unit(id = "knight", healthPoints = 10).toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
            val healedBattleUnit = battleUnit(unit = unit, player = player).toDto().copy(remainingHealthPoints = 10)
            givenBattleUnit(healedBattleUnit, unit, player)
            val searchPosition = mock<SearchPosition>()
            whenever(battlefieldApi.searchPosition).thenReturn(searchPosition)
            whenever(searchPosition(healedBattleUnit.id)).thenReturn(Battlefield.Dto.PositionDto(row = 0, column = 0))
            // When
            eventBus.publish(BattleUnitEvent.BattleUnitHealed(battleUnitId = healedBattleUnit.id, amount = 5, remainingHealthPoints = 10))
            eventBus.dispatch()
            // Then
            val state = argumentCaptor<UnitOverlayState>()
            verify(battlefieldView).displayUnitOverlay(eq(0), eq(0), state.capture())
            assertThat(state.firstValue.hpFraction).isEqualTo(1.0)
        }
    }

    @Nested
    inner class SelectBattleUnit {
        @Test
        fun `should display the unit info without ability buttons when an enemy battle unit is selected`() {
            // Given
            val unit = unit(id = "rat").toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.CPU).toDto()
            val enemyBattleUnit = battleUnit(unit = unit, player = player).toDto()
            givenBattleUnit(enemyBattleUnit, unit, player)
            // When
            eventBus.publish(
                BattlefieldHudEvent.SelectedBattleUnit(
                    battleUnitId = enemyBattleUnit.id,
                    tile = TileDto(row = 1, column = 1),
                    tilesWhereCanBeMoved = emptySet(),
                ),
            )
            eventBus.dispatch()
            // Then
            verify(battleUnitInfoView).display(enemyBattleUnit, unit, interactive = false, abilities = emptyList())
            verify(battlefieldView).displayUnitSelection(row = 1, column = 1, isEnemy = true)
        }

        @Test
        fun `should display the unit info with ability buttons when an ally battle unit is selected`() {
            // Given
            val unit = unit(id = "knight").toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
            val allyBattleUnit = battleUnit(unit = unit, player = player).toDto()
            givenBattleUnit(allyBattleUnit, unit, player)
            val availability =
                listOf(
                    AbilityAvailability("ability-1", "Sword", cost = 0, cooldownTurnsLeft = 2, status = AbilityAvailability.Status.COOLDOWN),
                )
            whenever(searchAbilityAvailability(allyBattleUnit.id)).thenReturn(availability)
            // When
            eventBus.publish(
                BattlefieldHudEvent.SelectedBattleUnit(
                    battleUnitId = allyBattleUnit.id,
                    tile = TileDto(row = 1, column = 1),
                    tilesWhereCanBeMoved = emptySet(),
                ),
            )
            eventBus.dispatch()
            // Then
            verify(battleUnitInfoView).display(allyBattleUnit, unit, interactive = true, abilities = availability)
            verify(battlefieldView).displayUnitSelection(row = 1, column = 1, isEnemy = false)
        }
    }

    @Nested
    inner class SelectAbility {
        @Test
        fun `should explain that there is no valid target when the selected ability has no cast group`() {
            // Given
            val unit = unit(id = "knight").toDto()
            val player = player(type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
            val allyBattleUnit = battleUnit(unit = unit, player = player).toDto()
            givenBattleUnit(allyBattleUnit, unit, player)
            whenever(abilityApi.searchAbilityById).thenReturn(mock())
            // When
            eventBus.publish(
                BattlefieldHudEvent.SelectedBattleUnitAbility(
                    casterTile = TileDto(row = 1, column = 1),
                    battleUnitId = allyBattleUnit.id,
                    abilityId = "ability-1",
                    castGroupsWhereCanCast = emptyList(),
                ),
            )
            eventBus.dispatch()
            // Then
            verify(battleUnitInfoView).displayAbilityLine("No valid target in reach")
            verify(battlefieldView).dimOutside(emptySet())
        }
    }

    private fun givenBattleUnit(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
        player: Player.Dto,
    ) {
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById(battleUnit.id)).thenReturn(battleUnit)
        val searchUnitById = mock<SearchUnitById>()
        whenever(unitApi.searchUnitById).thenReturn(searchUnitById)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        val searchPlayerById = mock<SearchPlayerById>()
        whenever(playerApi.searchPlayerById).thenReturn(searchPlayerById)
        whenever(searchPlayerById(player.id)).thenReturn(player)
    }
}
