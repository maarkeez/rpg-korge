package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PausePresenterTest {
    // The knight explores the sand, alone.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto()),
            tiles = List(2) { List(2) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 0, column = 0)),
            explore = true,
        ).also(SubdomainApis::initialise)
    private val pauseView = PauseView(TextsMother.texts())
    private val confirmDialogView = ConfirmDialogView()
    private var quits = 0
    private val presenter = PausePresenter(pauseView, confirmDialogView, apis.partyApi, TextsMother.texts(), battleSceneTimeline()) { quits++ }

    private fun openPause() {
        apis.partyApi.openPause()
        apis.eventBus.dispatch()
        presenter.open()
    }

    @Test
    fun `Open should display the pause screen when the game is paused`() {
        // When
        openPause()
        // Then
        presenter.isOpen.shouldBeTrue()
        pauseView.visible.shouldBeTrue()
        confirmDialogView.visible.shouldBeFalse()
    }

    @Test
    fun `PauseResumeSelected should explore again when Resume is tapped`() {
        // Given
        openPause()
        // When
        presenter.pauseResumeSelected()
        // Then
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    @Test
    fun `PauseQuitSelected should ask to confirm, as the progress since the last save is lost, when Quit to menu is tapped`() {
        // Given
        openPause()
        // When
        presenter.pauseQuitSelected()
        // Then
        pauseView.visible.shouldBeFalse()
        confirmDialogView.visible.shouldBeTrue()
        confirmDialogView.message() shouldBe "Quit to the menu? The progress since the last save is lost."
        confirmDialogView.choiceLabels().shouldContainExactly("Cancel", "Quit to menu")
        quits shouldBe 0
    }

    @Test
    fun `PauseQuitSelected should go back to the menu when quitting is confirmed`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        confirmDialogView.chooseConfirm()
        // Then
        quits shouldBe 1
    }

    @Test
    fun `PauseQuitSelected should display the pause screen again when quitting is cancelled`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        confirmDialogView.chooseCancel()
        // Then
        pauseView.visible.shouldBeTrue()
        confirmDialogView.visible.shouldBeFalse()
        quits shouldBe 0
    }

    @Test
    fun `Close should hide the pause screen and its dialog`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        presenter.close()
        // Then
        presenter.isOpen.shouldBeFalse()
        pauseView.visible.shouldBeFalse()
        confirmDialogView.visible.shouldBeFalse()
    }
}
