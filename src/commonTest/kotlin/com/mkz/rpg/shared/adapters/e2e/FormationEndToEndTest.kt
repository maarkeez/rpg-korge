package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickB
import com.mkz.rpg.screen.adapters.presentation.clickClose
import com.mkz.rpg.screen.adapters.presentation.crownedRowIndexes
import com.mkz.rpg.screen.adapters.presentation.formationView
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.names
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class FormationEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys the hero, a mage, at (2,1), alone in the party, out of reach of the CPU battle units.
    private val heroRow = KNIGHT_START_ROW
    private val heroColumn = KNIGHT_START_COLUMN

    @Test
    fun `should display the party formation in place of the virtual pad, the hero crowned, when the human player presses B while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(heroRow, heroColumn) == true }
            // When
            clickB(requireNotNull(virtualPadView()))
            // Then
            waitUntil { formationView()?.isShown() == true }
            val formationView = requireNotNull(formationView())
            formationView.names().shouldContainExactly("Mage")
            formationView.crownedRowIndexes().shouldContainExactly(0)
            withClue("Expected the virtual pad to be hidden while the formation is displayed") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
        }

    @Test
    fun `should display the virtual pad again when the human player closes the party formation`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(heroRow, heroColumn) == true }
            clickB(requireNotNull(virtualPadView()))
            waitUntil { formationView()?.isShown() == true }
            // When
            clickClose(requireNotNull(formationView()))
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the formation to be hidden") { formationView()?.isShown()?.shouldBeFalse() }
        }
}
