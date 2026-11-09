package com.mkz.rpg.screen.adapters.presentation.selection.domain

import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.tile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class SelectionTest {
    @Test
    fun `NothingSelected should publish the deselected event when the selection is created`() {
        // Given
        val selection = Selection.NothingSelected.create()
        // When
        val (events, _) = selection.pullEvents()
        // Then
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `NothingSelected should stay with nothing selected and publish the deselected event when deselect is invoked`() {
        // Given
        val selection = SelectionMother.nothingSelected()
        // When
        val updatedSelection = selection.deselect()
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.NothingSelected>()
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `NothingSelected should display the walking range and publish the event when a battle unit is selected`() {
        // Given
        val selection = SelectionMother.nothingSelected()
        val tile = tile(0, 0)
        val battleUnitId = "battle-unit-1"
        val walkingRange = setOf(tile(0, 1), tile(1, 0))
        // When
        val updatedSelection = selection.selectBattleUnit(tile = tile, battleUnitId = battleUnitId, walkingRange = walkingRange)
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.BattleUnitSelected>()
        val movementRange = updatedSelection as Selection.BattleUnitSelected
        movementRange.tile shouldBe tile
        movementRange.battleUnitId shouldBe battleUnitId
        movementRange.walkingRange shouldBe walkingRange
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.BattleUnitSelected(
                tile = tile,
                battleUnitId = battleUnitId,
                walkingRange = walkingRange,
            ),
        )
    }

    @Test
    fun `BattleUnitSelected should display the ability cast range and publish the event when an ability is selected`() {
        // Given
        val selection = SelectionMother.battleUnitSelected()
        val abilityId = "ability-1"
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)))
        // When
        val updatedSelection = selection.selectAbility(abilityId = abilityId, castGroupsWhereCanCast = castGroupsWhereCanCast)
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.AbilitySelected>()
        val castRange = updatedSelection as Selection.AbilitySelected
        castRange.casterTile shouldBe selection.tile
        castRange.battleUnitId shouldBe selection.battleUnitId
        castRange.abilityId shouldBe abilityId
        castRange.castGroupsWhereCanCast shouldBe castGroupsWhereCanCast
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.AbilitySelected(
                casterTile = selection.tile,
                battleUnitId = selection.battleUnitId,
                abilityId = abilityId,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `BattleUnitSelected should update the walking range when the tiles change`() {
        // Given
        val selection = SelectionMother.battleUnitSelected()
        val newTile = tile(1, 1)
        val newTilesWhereCanBeMoved = setOf(tile(1, 2))
        // When
        val updatedSelection = selection.refreshWalkingRange(tile = newTile, walkingRange = newTilesWhereCanBeMoved)
        // Then
        updatedSelection.tile shouldBe newTile
        updatedSelection.walkingRange shouldBe newTilesWhereCanBeMoved
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.BattleUnitSelected(
                tile = newTile,
                battleUnitId = selection.battleUnitId,
                walkingRange = newTilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `BattleUnitSelected should deselect and publish the deselected event when deselect is invoked`() {
        // Given
        val selection = SelectionMother.battleUnitSelected()
        // When
        val updatedSelection = selection.deselect()
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.NothingSelected>()
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `AbilitySelected should update the selected ability and publish the event when another ability is selected`() {
        // Given
        val selection = SelectionMother.abilitySelected()
        val newAbilityId = "ability-2"
        val newCastGroupsWhereCanCast = listOf(castGroup(tile(2, 2)))
        // When
        val updatedSelection = selection.selectAbility(abilityId = newAbilityId, castGroupsWhereCanCast = newCastGroupsWhereCanCast)
        // Then
        updatedSelection.abilityId shouldBe newAbilityId
        updatedSelection.castGroupsWhereCanCast shouldBe newCastGroupsWhereCanCast
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.AbilitySelected(
                casterTile = selection.casterTile,
                battleUnitId = selection.battleUnitId,
                abilityId = newAbilityId,
                castGroupsWhereCanCast = newCastGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `AbilitySelected should display the walking range and publish the events when the ability is deselected`() {
        // Given
        val selection = SelectionMother.abilitySelected()
        // When
        val updatedSelection = selection.deselectAbility()
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.BattleUnitSelected>()
        val movementRange = updatedSelection as Selection.BattleUnitSelected
        movementRange.tile shouldBe selection.casterTile
        movementRange.battleUnitId shouldBe selection.battleUnitId
        movementRange.walkingRange shouldBe selection.walkingRange
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactlyInAnyOrder(
            SelectionEvent.AbilityDeselected(abilityId = selection.abilityId),
            SelectionEvent.BattleUnitSelected(
                tile = selection.casterTile,
                battleUnitId = selection.battleUnitId,
                walkingRange = selection.walkingRange,
            ),
        )
    }

    @Test
    fun `AbilitySelected should preview the self ability cast and publish the event when a cast group is chosen`() {
        // Given
        val selection = SelectionMother.abilitySelected()
        val castGroup = castGroup(tile(1, 1))
        // When
        val updatedSelection = selection.previewCastOnCasterOrVacantTile(castGroup = castGroup)
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.CastPreviewed>()
        val preview = updatedSelection as Selection.CastPreviewed
        preview.casterTile shouldBe selection.casterTile
        preview.abilityId shouldBe selection.abilityId
        preview.castGroup shouldBe castGroup
        preview.recipientBattleUnitIds.shouldBeEmpty()
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.CastOnCasterOrVacantTilePreviewed(
                casterBattleUnitId = selection.battleUnitId,
                abilityId = selection.abilityId,
                castGroup = castGroup,
            ),
        )
    }

    @Test
    fun `AbilitySelected should preview the enemy ability cast and publish the event when an enemy cast group is chosen`() {
        // Given
        val selection = SelectionMother.abilitySelected()
        val castGroup = castGroup(tile(2, 2), tile(2, 3))
        val recipientBattleUnitIds = listOf("battle-unit-2", "battle-unit-3")
        // When
        val updatedSelection = selection.previewCastOnBattleUnits(castGroup = castGroup, recipientBattleUnitIds = recipientBattleUnitIds)
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.CastPreviewed>()
        val preview = updatedSelection as Selection.CastPreviewed
        preview.castGroup shouldBe castGroup
        preview.recipientBattleUnitIds shouldBe recipientBattleUnitIds
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.CastOnBattleUnitsPreviewed(
                casterBattleUnitId = selection.battleUnitId,
                abilityId = selection.abilityId,
                castGroup = castGroup,
                recipientBattleUnitIds = recipientBattleUnitIds,
            ),
        )
    }

    @Test
    fun `AbilitySelected should deselect and publish the deselected event when deselect is invoked`() {
        // Given
        val selection = SelectionMother.abilitySelected()
        // When
        val updatedSelection = selection.deselect()
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.NothingSelected>()
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `BattleUnitSelected should display the ability info when it displays the info of an ability`() {
        // Given
        val selection = SelectionMother.battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1")
        // When
        val updatedSelection = selection.displayAbilityInfo("skull")
        // Then
        updatedSelection.abilityId shouldBe "skull"
        updatedSelection.walkingRange shouldBe selection.walkingRange
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "skull"))
    }

    @Test
    fun `AbilitySelected should display the ability info when it displays the info of another ability`() {
        // Given
        val selection = SelectionMother.abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "sword")
        // When
        val updatedSelection = selection.displayAbilityInfo("mending")
        // Then
        updatedSelection.abilityId shouldBe "mending"
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "mending"))
    }

    @Test
    fun `AbilityInfoDisplayed should go back to the walking range when it hides the ability info`() {
        // Given
        val walkingRange = setOf(tile(0, 1))
        val selection = SelectionMother.abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "skull", walkingRange = walkingRange)
        // When
        val updatedSelection = selection.hideAbilityInfo()
        // Then
        updatedSelection.tile shouldBe tile(0, 0)
        updatedSelection.walkingRange shouldBe walkingRange
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.AbilityDeselected(abilityId = "skull"),
            SelectionEvent.BattleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `AbilityInfoDisplayed should display the cast range when it selects a castable ability`() {
        // Given
        val castGroups = listOf(castGroup(tile(0, 1)))
        val selection = SelectionMother.abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "skull")
        // When
        val updatedSelection = selection.selectAbility(abilityId = "sword", castGroupsWhereCanCast = castGroups)
        // Then
        updatedSelection.abilityId shouldBe "sword"
        updatedSelection.castGroupsWhereCanCast shouldBe castGroups
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(
            SelectionEvent.AbilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "sword", castGroupsWhereCanCast = castGroups),
        )
    }

    @Test
    fun `AbilityInfoDisplayed should display the info of another ability when it displays the info of another ability`() {
        // Given
        val selection = SelectionMother.abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "skull")
        // When
        val updatedSelection = selection.displayAbilityInfo("bat")
        // Then
        updatedSelection.abilityId shouldBe "bat"
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "bat"))
    }

    @Test
    fun `AbilityInfoDisplayed should deselect and publish the deselected event when deselect is invoked`() {
        // Given
        val selection = SelectionMother.abilityInfoDisplayed()
        // When
        val updatedSelection = selection.deselect()
        // Then
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `CastPreviewed should deselect and publish the deselected event when deselect is invoked`() {
        // Given
        val selection = SelectionMother.castPreviewed()
        // When
        val updatedSelection = selection.deselect()
        // Then
        updatedSelection.shouldBeInstanceOf<Selection.NothingSelected>()
        val (events, _) = updatedSelection.pullEvents()
        events.shouldContainExactly(SelectionEvent.Deselected)
    }

    @Test
    fun `CastPreviewed should display the caster walking range without publishing events when the cast is confirmed`() {
        // Given
        val selection =
            SelectionMother.castPreviewed(
                casterTile = tile(2, 2),
                battleUnitId = "battle-unit-1",
                walkingRange = setOf(tile(2, 3)),
            )
        // When
        val updatedSelection = selection.confirmCast()
        // Then
        updatedSelection shouldBe SelectionMother.battleUnitSelected(tile = tile(2, 2), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(2, 3)))
        updatedSelection.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `CastPreviewed should display the ability cast range again and publish the event when the cast is cancelled`() {
        // Given
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)), castGroup(tile(0, 1)))
        val selection =
            SelectionMother.castPreviewed(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        // When
        val updatedSelection = selection.cancelCast()
        // Then
        val (events, selectionWithoutEvents) = updatedSelection.pullEvents()
        selectionWithoutEvents shouldBe
            SelectionMother.abilitySelected(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        events.shouldContainExactly(
            SelectionEvent.AbilitySelected(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `CastPreviewed should pull the pending events and clear them when the preview has events`() {
        // Given
        val selection = SelectionMother.abilitySelected().previewCastOnCasterOrVacantTile(castGroup = castGroup(tile(1, 1)))
        // When
        val (events, clearedSelection) = selection.pullEvents()
        // Then
        events.shouldNotBeEmpty()
        clearedSelection.pullEvents().first.shouldBeEmpty()
    }
}
