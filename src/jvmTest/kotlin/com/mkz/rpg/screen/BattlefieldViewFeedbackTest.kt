package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.feedback.FxViews
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import korlibs.image.bitmap.BmpSlice
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.View
import korlibs.korge.view.descendantsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattlefieldViewFeedbackTest : ViewsForTesting() {
    private suspend fun displayedBattlefieldView(): BattlefieldView {
        val battlefieldView = BattlefieldView()
        battlefieldView.loadAssets()
        battlefieldView.displayBattlefield(battlefield(rows = 3, columns = 4).toDto())
        return battlefieldView
    }

    private fun BattlefieldView.fxNamed(name: String): List<View> = descendantsWith { it.name == name }

    private fun BattlefieldView.tile(
        row: Int,
        column: Int,
    ): UIButton = descendantsWith { it.name == "row-$row-column-$column" }.first() as UIButton

    @Nested
    inner class PlayHitFlash {
        @Test
        fun `should show a white copy of the sprite on the tile for two frames when a hit flashes`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                // When
                battlefieldView.playHitFlash(row = 1, column = 2, unitId = "knight")
                // Then
                val flash = battlefieldView.fxNamed(FxViews.FLASH_NAME).single()
                assertThat(flash.x).isEqualTo(2.0 * BattlefieldView.TILE_SIZE)
                assertThat(flash.y).isEqualTo(1.0 * BattlefieldView.TILE_SIZE)
                assertThat(FxViews.FLASH_MS).isLessThanOrEqualTo(2 * 67)
            }

        @Test
        fun `should remove the flash when its time has passed`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.playHitFlash(row = 0, column = 0, unitId = "knight")
                // When
                battlefieldView.advanceFx(deltaMs = FxViews.FLASH_MS.toDouble())
                // Then
                assertThat(battlefieldView.activeFxCount).isZero()
                assertThat(battlefieldView.fxNamed(FxViews.FLASH_NAME)).isEmpty()
            }

        @Test
        fun `should not touch the unit sprite on the tile when a hit flashes`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.displayKnightBattleUnit(row = 0, column = 0)
                // When
                battlefieldView.playHitFlash(row = 0, column = 0, unitId = "knight")
                // Then
                assertThat(battlefieldView.tile(0, 0).findViewByName(BattlefieldView.BATTLE_UNIT)).isNotNull
            }
    }

    @Nested
    inner class PlayAmountPop {
        @Test
        fun `should rise in whole art pixels and then disappear when an amount pops`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.playAmountPop(row = 1, column = 1, amount = 12, heal = false)
                val number = battlefieldView.fxNamed(FxViews.NUMBER_NAME).single()
                val startY = number.y
                // When
                battlefieldView.advanceFx(deltaMs = FxViews.NUMBER_MS * 0.6)
                // Then
                val risen = startY - number.y
                assertThat(risen).isPositive()
                assertThat(risen % PIXEL_SCALE).isZero()
                battlefieldView.advanceFx(deltaMs = FxViews.NUMBER_MS.toDouble())
                assertThat(battlefieldView.fxNamed(FxViews.NUMBER_NAME)).isEmpty()
            }

        @Test
        fun `should use the damage color for damage and the heal color preceded by a plus for healing`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                // When
                battlefieldView.playAmountPop(row = 0, column = 0, amount = 5, heal = false)
                battlefieldView.playAmountPop(row = 0, column = 1, amount = 5, heal = true)
                // Then
                val numbers = battlefieldView.fxNamed(FxViews.NUMBER_NAME)
                val damage = numbers.first { it.x < BattlefieldView.TILE_SIZE }
                val heal = numbers.first { it.x >= BattlefieldView.TILE_SIZE }
                assertThat((heal as Container).numChildren).isGreaterThan((damage as Container).numChildren)
            }
    }

    @Nested
    inner class PlayPoof {
        @Test
        fun `should expand over the tile in frames and then disappear when a unit is defeated`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.playPoof(row = 2, column = 3)
                val poof = battlefieldView.fxNamed(FxViews.POOF_NAME).single() as Container
                val firstFrame = poof.frameSignature()
                // When
                battlefieldView.advanceFx(deltaMs = FxViews.POOF_MS * 0.9)
                // Then
                assertThat(poof.frameSignature()).isNotEqualTo(firstFrame)
                battlefieldView.advanceFx(deltaMs = FxViews.POOF_MS.toDouble())
                assertThat(battlefieldView.fxNamed(FxViews.POOF_NAME)).isEmpty()
            }
    }

    @Nested
    inner class PlayStatusPop {
        @Test
        fun `should show a sparkle at the pips of the unit and then remove it when a status arrives`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                // When
                battlefieldView.playStatusPop(row = 0, column = 2)
                // Then
                assertThat(battlefieldView.fxNamed(FxViews.POP_NAME)).hasSize(1)
                battlefieldView.advanceFx(deltaMs = FxViews.POP_MS.toDouble())
                assertThat(battlefieldView.fxNamed(FxViews.POP_NAME)).isEmpty()
            }
    }

    @Nested
    inner class PlaySpark {
        @Test
        fun `should travel from the center of one tile to the center of the other in whole art pixels when a spread plays`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.playSpark(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, durationMs = 200)
                val spark = battlefieldView.fxNamed(FxViews.SPARK_NAME).single()
                val startX = spark.x
                // When
                battlefieldView.advanceFx(deltaMs = 100.0)
                // Then
                assertThat(spark.x).isGreaterThan(startX)
                assertThat(spark.x % PIXEL_SCALE).isZero()
                battlefieldView.advanceFx(deltaMs = 100.0)
                assertThat(battlefieldView.fxNamed(FxViews.SPARK_NAME)).isEmpty()
            }
    }

    @Nested
    inner class ShowWalker {
        @Test
        fun `should draw the walking unit without touching the tile content when a unit walks through an occupied tile`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.displayRatBattleUnit(row = 0, column = 1)
                // When
                battlefieldView.showWalker(unitType = "knight", row = 0, column = 1, walkerId = "walker-1")
                // Then
                assertThat(battlefieldView.isWalking("walker-1")).isTrue()
                assertThat(battlefieldView.descendantsWith { it.name == BattlefieldView.BATTLE_UNIT && it.parent == battlefieldView.tile(0, 1) }).hasSize(1)
            }

        @Test
        fun `should stop drawing the walking unit when it arrives`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.showWalker(unitType = "knight", row = 0, column = 1, walkerId = "walker-1")
                // When
                battlefieldView.hideWalker("walker-1")
                // Then
                assertThat(battlefieldView.isWalking("walker-1")).isFalse()
                assertThat(battlefieldView.fxNamed(FxViews.WALKER_NAME)).isEmpty()
            }

        @Test
        fun `should show the next walk frame when the walker reaches the next tile`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.showWalker(unitType = "knight", row = 0, column = 0)
                val walker = battlefieldView.fxNamed(FxViews.WALKER_NAME).single() as Container
                val firstStep = ((walker.firstChild as Image).bitmap as BmpSlice).base
                // When
                battlefieldView.showWalker(unitType = "knight", row = 0, column = 1)
                // Then
                assertThat(((walker.firstChild as Image).bitmap as BmpSlice).base).isNotSameAs(firstStep)
            }
    }

    @Nested
    inner class RemoveAllBattleUnits {
        @Test
        fun `should remove every unit sprite and overlay when all battle units are removed`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.displayKnightBattleUnit(row = 0, column = 0)
                battlefieldView.displayRatBattleUnit(row = 2, column = 3)
                battlefieldView.displayUnitOverlay(
                    row = 2,
                    column = 3,
                    state = UnitOverlayState(10, 10, isEnemy = true, onTurnStartedEffectCount = 0, onDefeatedEffectCount = 0),
                )
                // When
                battlefieldView.removeAllBattleUnits()
                // Then
                assertThat(battlefieldView.descendantsWith { it.name == BattlefieldView.BATTLE_UNIT }).isEmpty()
                assertThat(battlefieldView.descendantsWith { it.name == BattlefieldView.UNIT_OVERLAY }).isEmpty()
            }
    }

    @Nested
    inner class ClearFx {
        @Test
        fun `should remove running effects and walkers when effects are cleared`() =
            viewsTest {
                // Given
                val battlefieldView = displayedBattlefieldView()
                battlefieldView.playPoof(row = 0, column = 0)
                battlefieldView.showWalker(unitType = "knight", row = 0, column = 1, walkerId = "walker-1")
                // When
                battlefieldView.clearFx()
                // Then
                assertThat(battlefieldView.activeFxCount).isZero()
                assertThat(battlefieldView.isWalking("walker-1")).isFalse()
            }
    }

    @Nested
    inner class Scrolling {
        @Test
        fun `should keep effects aligned with the tiles when the battlefield scrolls`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                // When
                battlefieldView.centerOn(row = 10, column = 10)
                battlefieldView.playHitFlash(row = 10, column = 10, unitId = "knight")
                // Then
                val flash = battlefieldView.fxNamed(FxViews.FLASH_NAME).single()
                val tile = battlefieldView.tile(10, 10)
                assertThat(flash.parent!!.x + flash.x).isEqualTo(tile.parent!!.x + tile.x)
                assertThat(flash.parent!!.y + flash.y).isEqualTo(tile.parent!!.y + tile.y)
            }
    }

    @Nested
    inner class IdleAnimation {
        private suspend fun displayedKnight(idleFrameMs: Int): Pair<BattlefieldView, Image> {
            val battlefieldView = BattlefieldView(idleFrameMs = idleFrameMs)
            battlefieldView.loadAssets()
            battlefieldView.displayBattlefield(battlefield(rows = 3, columns = 4).toDto())
            battlefieldView.displayKnightBattleUnit(row = 1, column = 1)
            val knight = battlefieldView.tile(1, 1).descendantsWith { it.name == BattlefieldView.BATTLE_UNIT }.single() as Image
            return battlefieldView to knight
        }

        @Test
        fun `should show the next idle frame when an idle frame time has passed`() =
            viewsTest {
                // Given
                val (battlefieldView, knight) = displayedKnight(idleFrameMs = 500)
                val firstFrame = (knight.bitmap as BmpSlice).base
                // When
                battlefieldView.advanceFx(deltaMs = 500.0)
                // Then
                assertThat((knight.bitmap as BmpSlice).base).isNotSameAs(firstFrame)
            }

        @Test
        fun `should keep the unit on frame 0 when idle animation is disabled`() =
            viewsTest {
                // Given
                val (battlefieldView, knight) = displayedKnight(idleFrameMs = 0)
                val firstFrame = (knight.bitmap as BmpSlice).base
                // When
                battlefieldView.advanceFx(deltaMs = 5_000.0)
                // Then
                assertThat((knight.bitmap as BmpSlice).base).isSameAs(firstFrame)
            }
    }
}

// Identifies the drawn poof frame: the bitmap of an authored frame, or the pixels of the procedural ring.
private fun Container.frameSignature(): List<Any?> = children.map { (it as? Image)?.bitmap ?: (it.x to it.y) }
