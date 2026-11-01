package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.save.domain.SaveEvent.GameRestored
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestPresenterTest {
    private val questId = "defeat-the-goat-wizard"
    private val trackerView = FakeQuestTrackerView()

    // The knight explores the sand, alone, with the quests given.
    private fun apis(vararg quests: Quest) =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight").toDto(), unit(id = "rat").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(2) { List(2) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 0, column = 0)),
            quests = quests.map(Quest::toDto),
            explore = true,
        ).also(SubdomainApis::initialise)

    private fun SubdomainApis.presenter() = QuestPresenter(trackerView, questApi, TextsMother.texts(), battleSceneTimeline(), eventBus)

    private fun SubdomainApis.startQuest() {
        questApi.startMainQuest()
        eventBus.dispatch()
    }

    private fun SubdomainApis.defeatRat() {
        eventBus.publish(BattleUnitDefeated(playerId = "cpu", battleUnitId = "rat-1", unitId = "rat", defeatedAtRow = 1, defeatedAtColumn = 1))
        eventBus.dispatch()
    }

    @Test
    fun `QuestPresenter should draw the tracker when the main quest starts`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        apis.presenter()
        // When
        apis.startQuest()
        // Then
        trackerView.displayedQuests
            .single()
            .currentTask.text shouldBe "Defeat the Goat Wizard"
        trackerView.pops shouldBe 0
    }

    @Test
    fun `QuestPresenter should ignore an event when it names a side quest`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        apis.presenter()
        // When
        apis.eventBus.publish(QuestStarted(questId = "side"))
        apis.eventBus.dispatch()
        // Then
        trackerView.displayedQuests.shouldBeEmpty()
    }

    @Test
    fun `QuestPresenter should pop the tracker when a defeat counts`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 5))))
        apis.presenter()
        apis.startQuest()
        // When
        apis.defeatRat()
        // Then
        trackerView.displayedQuests
            .last()
            .currentTask.progress shouldBe 1
        trackerView.pops shouldBe 1
    }

    @Test
    fun `QuestPresenter should pop the tracker once when a defeat completes a task with another one after it`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 1), defeatUnits(unitId = "knight", count = 1))))
        apis.presenter()
        apis.startQuest()
        // When
        apis.defeatRat()
        // Then
        trackerView.displayedQuests
            .last()
            .currentTask.portraitPath shouldBe "unit/knight_portrait.png"
        trackerView.displayedQuests
            .last()
            .doneTasks.size shouldBe 1
        trackerView.displayedQuests.size shouldBe 2
        trackerView.pops shouldBe 1
    }

    @Test
    fun `QuestPresenter should tick the tracker once when the last defeat publishes three events`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 1))))
        apis.presenter()
        apis.startQuest()
        // When
        apis.defeatRat()
        // Then
        trackerView.displayedQuests.size shouldBe 2
        trackerView.displayedQuests
            .last()
            .isCompleted
            .shouldBeTrue()
        trackerView.pops shouldBe 0
    }

    @Test
    fun `QuestPresenter should draw the tracker without popping when a game is restored`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        apis.presenter()
        // When
        apis.eventBus.publish(GameRestored(slot = 1))
        apis.eventBus.dispatch()
        // Then
        trackerView.displayedQuests
            .single()
            .isCompleted
            .shouldBeFalse()
        trackerView.pops shouldBe 0
    }

    @Test
    fun `QuestPresenter should make the tracker read only when a battle starts, and tappable again when it finishes`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        apis.presenter()
        // When
        apis.eventBus.publish(ExplorationPaused(playerId = "human"))
        apis.eventBus.publish(ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1")))
        apis.eventBus.dispatch()
        // Then
        trackerView.tappableChanges.shouldContainExactly(false, true)
    }

    @Test
    fun `QuestPresenter should hide the tracker when a player wins`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        apis.presenter()
        // When
        apis.eventBus.publish(PlayerVictory(playerId = "human"))
        apis.eventBus.dispatch()
        // Then
        trackerView.hides shouldBe 1
    }
}
