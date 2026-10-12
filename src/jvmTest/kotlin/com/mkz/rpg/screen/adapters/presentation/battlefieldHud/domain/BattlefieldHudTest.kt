package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class BattlefieldHudTest {
    @Test
    fun `Idle should publish the idle event when the hud is created`() {
        // Given
        val hud = BattlefieldHud.Idle.create()
        // When
        val (events, _) = hud.pullEvents()
        // Then
        events.shouldContainExactly(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `Idle should stay idle and publish the idle event when idle is invoked`() {
        // Given
        val hud = BattlefieldHudMother.idle()
        // When
        val updatedHud = hud.idle()
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.Idle>()
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `Idle should display the movement range and publish the event when a battle unit is selected`() {
        // Given
        val hud = BattlefieldHudMother.idle()
        val tile = tile(0, 0)
        val battleUnitId = "battle-unit-1"
        val tilesWhereCanBeMoved = setOf(tile(0, 1), tile(1, 0))
        // When
        val updatedHud = hud.selectBattleUnit(tile = tile, battleUnitId = battleUnitId, tilesWhereCanBeMoved = tilesWhereCanBeMoved)
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.DisplayMovementRange>()
        val movementRange = updatedHud as BattlefieldHud.DisplayMovementRange
        movementRange.tile shouldBe tile
        movementRange.battleUnitId shouldBe battleUnitId
        movementRange.tilesWhereCanBeMoved shouldBe tilesWhereCanBeMoved
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = tile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `DisplayMovementRange should display the ability cast range and publish the event when an ability is selected`() {
        // Given
        val hud = BattlefieldHudMother.displayMovementRange()
        val abilityId = "ability-1"
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)))
        // When
        val updatedHud = hud.selectAbility(abilityId = abilityId, castGroupsWhereCanCast = castGroupsWhereCanCast)
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.DisplayAbilityCastRange>()
        val castRange = updatedHud as BattlefieldHud.DisplayAbilityCastRange
        castRange.casterTile shouldBe hud.tile
        castRange.battleUnitId shouldBe hud.battleUnitId
        castRange.abilityId shouldBe abilityId
        castRange.castGroupsWhereCanCast shouldBe castGroupsWhereCanCast
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = hud.tile,
                battleUnitId = hud.battleUnitId,
                abilityId = abilityId,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `DisplayMovementRange should update the tiles where the battle unit can be moved when the tiles change`() {
        // Given
        val hud = BattlefieldHudMother.displayMovementRange()
        val newTile = tile(1, 1)
        val newTilesWhereCanBeMoved = setOf(tile(1, 2))
        // When
        val updatedHud = hud.tilesWhereCanBeMoved(tile = newTile, tilesWhereCanBeMoved = newTilesWhereCanBeMoved)
        // Then
        updatedHud.tile shouldBe newTile
        updatedHud.tilesWhereCanBeMoved shouldBe newTilesWhereCanBeMoved
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = newTile,
                battleUnitId = hud.battleUnitId,
                tilesWhereCanBeMoved = newTilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `DisplayMovementRange should return to idle and publish the idle event when idle is invoked`() {
        // Given
        val hud = BattlefieldHudMother.displayMovementRange()
        // When
        val updatedHud = hud.idle()
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.Idle>()
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `DisplayAbilityCastRange should update the selected ability and publish the event when another ability is selected`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange()
        val newAbilityId = "ability-2"
        val newCastGroupsWhereCanCast = listOf(castGroup(tile(2, 2)))
        // When
        val updatedHud = hud.selectAbility(abilityId = newAbilityId, castGroupsWhereCanCast = newCastGroupsWhereCanCast)
        // Then
        updatedHud.abilityId shouldBe newAbilityId
        updatedHud.castGroupsWhereCanCast shouldBe newCastGroupsWhereCanCast
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = hud.casterTile,
                battleUnitId = hud.battleUnitId,
                abilityId = newAbilityId,
                castGroupsWhereCanCast = newCastGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `DisplayAbilityCastRange should display the movement range and publish the events when the ability is deselected`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange()
        // When
        val updatedHud = hud.deselectAbility()
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.DisplayMovementRange>()
        val movementRange = updatedHud as BattlefieldHud.DisplayMovementRange
        movementRange.tile shouldBe hud.casterTile
        movementRange.battleUnitId shouldBe hud.battleUnitId
        movementRange.tilesWhereCanBeMoved shouldBe hud.tilesWhereCanBeMoved
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactlyInAnyOrder(
            BattlefieldHudEvent.AbilityDeselected(abilityId = hud.abilityId),
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = hud.casterTile,
                battleUnitId = hud.battleUnitId,
                tilesWhereCanBeMoved = hud.tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `DisplayAbilityCastRange should preview the self ability cast and publish the event when a cast group is chosen`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange()
        val castGroup = castGroup(tile(1, 1))
        // When
        val updatedHud = hud.previewSelfAbilityCast(castGroup = castGroup)
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.DisplayAbilityCastPreview>()
        val preview = updatedHud as BattlefieldHud.DisplayAbilityCastPreview
        preview.casterTile shouldBe hud.casterTile
        preview.abilityId shouldBe hud.abilityId
        preview.castGroup shouldBe castGroup
        preview.enemyBattleUnitId.shouldBeNull()
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = hud.battleUnitId,
                abilityId = hud.abilityId,
                castGroup = castGroup,
            ),
        )
    }

    @Test
    fun `DisplayAbilityCastRange should preview the enemy ability cast and publish the event when an enemy cast group is chosen`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange()
        val castGroup = castGroup(tile(2, 2))
        val enemyBattleUnitId = "battle-unit-2"
        // When
        val updatedHud = hud.previewEnemyAbilityCast(castGroup = castGroup, enemyBattleUnitId = enemyBattleUnitId)
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.DisplayAbilityCastPreview>()
        val preview = updatedHud as BattlefieldHud.DisplayAbilityCastPreview
        preview.castGroup shouldBe castGroup
        preview.enemyBattleUnitId shouldBe enemyBattleUnitId
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = hud.battleUnitId,
                abilityId = hud.abilityId,
                castGroup = castGroup,
                enemyBattleUnitId = enemyBattleUnitId,
            ),
        )
    }

    @Test
    fun `DisplayAbilityCastRange should return to idle and publish the idle event when idle is invoked`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange()
        // When
        val updatedHud = hud.idle()
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.Idle>()
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `DisplayAbilityCastPreview should return to idle and publish the idle event when idle is invoked`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastPreview()
        // When
        val updatedHud = hud.idle()
        // Then
        updatedHud.shouldBeInstanceOf<BattlefieldHud.Idle>()
        val (events, _) = updatedHud.pullEvents()
        events.shouldContainExactly(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `DisplayAbilityCastPreview should display the caster movement range without publishing events when the cast is confirmed`() {
        // Given
        val hud =
            BattlefieldHudMother.displayAbilityCastPreview(
                casterTile = tile(2, 2),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = setOf(tile(2, 3)),
            )
        // When
        val updatedHud = hud.confirmCast()
        // Then
        updatedHud shouldBe BattlefieldHudMother.displayMovementRange(tile = tile(2, 2), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = setOf(tile(2, 3)))
        updatedHud.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `DisplayAbilityCastPreview should display the ability cast range again and publish the event when the cast is cancelled`() {
        // Given
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)), castGroup(tile(0, 1)))
        val hud =
            BattlefieldHudMother.displayAbilityCastPreview(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        // When
        val updatedHud = hud.cancelCast()
        // Then
        val (events, hudWithoutEvents) = updatedHud.pullEvents()
        hudWithoutEvents shouldBe
            BattlefieldHudMother.displayAbilityCastRange(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        events.shouldContainExactly(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `DisplayAbilityCastPreview should pull the pending events and clear them when the preview has events`() {
        // Given
        val hud = BattlefieldHudMother.displayAbilityCastRange().previewSelfAbilityCast(castGroup = castGroup(tile(1, 1)))
        // When
        val (events, clearedHud) = hud.pullEvents()
        // Then
        events.shouldNotBeEmpty()
        clearedHud.pullEvents().first.shouldBeEmpty()
    }
}
