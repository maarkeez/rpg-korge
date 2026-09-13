package com.mkz.rpg.screen.battlefieldHud.domain

import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.tile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattlefieldHudTest {
    @Nested
    inner class Idle {
        @Test
        fun `should publish the idle event when the hud is created`() {
            // Given
            val hud = BattlefieldHud.Idle.create()
            // When
            val (events, _) = hud.pullEvents()
            // Then
            assertThat(events).containsExactly(BattlefieldHudEvent.Idle)
        }

        @Test
        fun `should stay idle and publish the idle event when idle is invoked`() {
            // Given
            val hud = BattlefieldHudMother.idle()
            // When
            val updatedHud = hud.idle()
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.Idle::class.java)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events).containsExactly(BattlefieldHudEvent.Idle)
        }

        @Test
        fun `should display the movement range and publish the event when a battle unit is selected`() {
            // Given
            val hud = BattlefieldHudMother.idle()
            val tile = tile(0, 0)
            val battleUnitId = "battle-unit-1"
            val tilesWhereCanBeMoved = setOf(tile(0, 1), tile(1, 0))
            // When
            val updatedHud = hud.selectBattleUnit(tile = tile, battleUnitId = battleUnitId, tilesWhereCanBeMoved = tilesWhereCanBeMoved)
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.DisplayMovementRange::class.java)
            val movementRange = updatedHud as BattlefieldHud.DisplayMovementRange
            assertThat(movementRange.tile).isEqualTo(tile)
            assertThat(movementRange.battleUnitId).isEqualTo(battleUnitId)
            assertThat(movementRange.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.SelectedBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                    ),
                )
        }
    }

    @Nested
    inner class DisplayMovementRange {
        @Test
        fun `should display the ability cast range and publish the event when an ability is selected`() {
            // Given
            val hud = BattlefieldHudMother.displayMovementRange()
            val abilityId = "ability-1"
            val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)))
            // When
            val updatedHud = hud.selectAbility(abilityId = abilityId, castGroupsWhereCanCast = castGroupsWhereCanCast)
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.DisplayAbilityCastRange::class.java)
            val castRange = updatedHud as BattlefieldHud.DisplayAbilityCastRange
            assertThat(castRange.casterTile).isEqualTo(hud.tile)
            assertThat(castRange.battleUnitId).isEqualTo(hud.battleUnitId)
            assertThat(castRange.abilityId).isEqualTo(abilityId)
            assertThat(castRange.castGroupsWhereCanCast).isEqualTo(castGroupsWhereCanCast)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.SelectedBattleUnitAbility(
                        casterTile = hud.tile,
                        battleUnitId = hud.battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
                )
        }

        @Test
        fun `should update the tiles where the battle unit can be moved when the tiles change`() {
            // Given
            val hud = BattlefieldHudMother.displayMovementRange()
            val newTile = tile(1, 1)
            val newTilesWhereCanBeMoved = setOf(tile(1, 2))
            // When
            val updatedHud = hud.tilesWhereCanBeMoved(tile = newTile, tilesWhereCanBeMoved = newTilesWhereCanBeMoved)
            // Then
            assertThat(updatedHud.tile).isEqualTo(newTile)
            assertThat(updatedHud.tilesWhereCanBeMoved).isEqualTo(newTilesWhereCanBeMoved)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.SelectedBattleUnit(
                        tile = newTile,
                        battleUnitId = hud.battleUnitId,
                        tilesWhereCanBeMoved = newTilesWhereCanBeMoved,
                    ),
                )
        }

        @Test
        fun `should return to idle and publish the idle event when idle is invoked`() {
            // Given
            val hud = BattlefieldHudMother.displayMovementRange()
            // When
            val updatedHud = hud.idle()
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.Idle::class.java)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events).containsExactly(BattlefieldHudEvent.Idle)
        }
    }

    @Nested
    inner class DisplayAbilityCastRange {
        @Test
        fun `should update the selected ability and publish the event when another ability is selected`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange()
            val newAbilityId = "ability-2"
            val newCastGroupsWhereCanCast = listOf(castGroup(tile(2, 2)))
            // When
            val updatedHud = hud.selectAbility(abilityId = newAbilityId, castGroupsWhereCanCast = newCastGroupsWhereCanCast)
            // Then
            assertThat(updatedHud.abilityId).isEqualTo(newAbilityId)
            assertThat(updatedHud.castGroupsWhereCanCast).isEqualTo(newCastGroupsWhereCanCast)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.SelectedBattleUnitAbility(
                        casterTile = hud.casterTile,
                        battleUnitId = hud.battleUnitId,
                        abilityId = newAbilityId,
                        castGroupsWhereCanCast = newCastGroupsWhereCanCast,
                    ),
                )
        }

        @Test
        fun `should display the movement range and publish the events when the ability is deselected`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange()
            // When
            val updatedHud = hud.deselectAbility()
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.DisplayMovementRange::class.java)
            val movementRange = updatedHud as BattlefieldHud.DisplayMovementRange
            assertThat(movementRange.tile).isEqualTo(hud.casterTile)
            assertThat(movementRange.battleUnitId).isEqualTo(hud.battleUnitId)
            assertThat(movementRange.tilesWhereCanBeMoved).isEqualTo(hud.tilesWhereCanBeMoved)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactlyInAnyOrder(
                    BattlefieldHudEvent.AbilityDeselected(abilityId = hud.abilityId),
                    BattlefieldHudEvent.SelectedBattleUnit(
                        tile = hud.casterTile,
                        battleUnitId = hud.battleUnitId,
                        tilesWhereCanBeMoved = hud.tilesWhereCanBeMoved,
                    ),
                )
        }

        @Test
        fun `should preview the self ability cast and publish the event when a cast group is chosen`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange()
            val castGroup = castGroup(tile(1, 1))
            // When
            val updatedHud = hud.previewSelfAbilityCast(castGroup = castGroup)
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.DisplayAbilityCastPreview::class.java)
            val preview = updatedHud as BattlefieldHud.DisplayAbilityCastPreview
            assertThat(preview.casterTile).isEqualTo(hud.casterTile)
            assertThat(preview.abilityId).isEqualTo(hud.abilityId)
            assertThat(preview.castGroup).isEqualTo(castGroup)
            assertThat(preview.enemyBattleUnitId).isNull()
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.SelfAbilityCastPreviewed(
                        casterBattleUnitId = hud.battleUnitId,
                        abilityId = hud.abilityId,
                        castGroup = castGroup,
                    ),
                )
        }

        @Test
        fun `should preview the enemy ability cast and publish the event when an enemy cast group is chosen`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange()
            val castGroup = castGroup(tile(2, 2))
            val enemyBattleUnitId = "battle-unit-2"
            // When
            val updatedHud = hud.previewEnemyAbilityCast(castGroup = castGroup, enemyBattleUnitId = enemyBattleUnitId)
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.DisplayAbilityCastPreview::class.java)
            val preview = updatedHud as BattlefieldHud.DisplayAbilityCastPreview
            assertThat(preview.castGroup).isEqualTo(castGroup)
            assertThat(preview.enemyBattleUnitId).isEqualTo(enemyBattleUnitId)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                        casterBattleUnitId = hud.battleUnitId,
                        abilityId = hud.abilityId,
                        castGroup = castGroup,
                        enemyBattleUnitId = enemyBattleUnitId,
                    ),
                )
        }

        @Test
        fun `should return to idle and publish the idle event when idle is invoked`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange()
            // When
            val updatedHud = hud.idle()
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.Idle::class.java)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events).containsExactly(BattlefieldHudEvent.Idle)
        }
    }

    @Nested
    inner class DisplayAbilityCastPreview {
        @Test
        fun `should return to idle and publish the idle event when idle is invoked`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastPreview()
            // When
            val updatedHud = hud.idle()
            // Then
            assertThat(updatedHud).isInstanceOf(BattlefieldHud.Idle::class.java)
            val (events, _) = updatedHud.pullEvents()
            assertThat(events).containsExactly(BattlefieldHudEvent.Idle)
        }

        @Test
        fun `should pull the pending events and clear them when the preview has events`() {
            // Given
            val hud = BattlefieldHudMother.displayAbilityCastRange().previewSelfAbilityCast(castGroup = castGroup(tile(1, 1)))
            // When
            val (events, clearedHud) = hud.pullEvents()
            // Then
            assertThat(events).isNotEmpty()
            assertThat(clearedHud.pullEvents().first).isEmpty()
        }
    }
}
