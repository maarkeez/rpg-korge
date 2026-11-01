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
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.save.domain.SaveEvent.GameRestored
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
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
    private val detailView = QuestDetailView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val journalView = QuestJournalView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val announcementView = FakeQuestAnnouncementView()
    private val timeline = battleSceneTimeline()

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

    private fun SubdomainApis.presenter() = QuestPresenter(trackerView, detailView, journalView, announcementView, questApi, partyApi, TextsMother.texts(), timeline, eventBus)

    private fun SubdomainApis.startQuest() {
        questApi.startMainQuest()
        eventBus.dispatch()
    }

    // The quest under way.
    private fun SubdomainApis.startedPresenter(): QuestPresenter {
        val presenter = presenter()
        startQuest()
        return presenter
    }

    private fun SubdomainApis.defeatRat() {
        eventBus.publish(BattleUnitDefeated(playerId = "cpu", battleUnitId = "rat-1", unitId = "rat", defeatedAtRow = 1, defeatedAtColumn = 1))
        eventBus.dispatch()
    }

    @Test
    fun `QuestPresenter should draw the tracker and announce when the main quest starts`() {
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
        announcementView.announcements.map { (moment, _) -> moment }.shouldContainExactly(Moment.STARTED)
    }

    @Test
    fun `QuestPresenter should announce a side quest when it starts and when it completes`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        apis.presenter()
        // When
        apis.eventBus.publish(QuestStarted(questId = "side"))
        apis.eventBus.publish(QuestCompleted(questId = "side"))
        apis.eventBus.dispatch()
        // Then
        announcementView.announcements.map { (moment, _) -> moment }.shouldContainExactly(Moment.STARTED, Moment.COMPLETED)
        trackerView.displayedQuests.shouldBeEmpty()
    }

    @Test
    fun `QuestPresenter should play the toasts one after the other when a talk starts two quests`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"), sideQuest(id = "other"))
        apis.presenter()
        announcementView.isHolding = true
        // When
        apis.eventBus.publish(QuestStarted(questId = "side"))
        apis.eventBus.publish(QuestStarted(questId = "other"))
        apis.eventBus.dispatch()
        // Then
        announcementView.announcements.size shouldBe 1
        // When
        announcementView.fadeToast()
        // Then
        announcementView.announcements.size shouldBe 2
        // When
        announcementView.fadeToast()
        apis.eventBus.publish(QuestCompleted(questId = "side"))
        apis.eventBus.dispatch()
        // Then: with none left, the next one plays straight away.
        announcementView.announcements.size shouldBe 3
    }

    @Test
    fun `QuestPresenter should not hold the timeline when it announces`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        apis.presenter()
        announcementView.isHolding = true
        // When
        apis.startQuest()
        // Then
        announcementView.announcements.size shouldBe 1
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `QuestPresenter should pop the tracker when a defeat counts`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 5))))
        apis.startedPresenter()
        // When
        apis.defeatRat()
        // Then
        trackerView.displayedQuests
            .last()
            .currentTask.progress shouldBe 1
        trackerView.pops shouldBe 1
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `QuestPresenter should pop the tracker once when a defeat completes a task with another one after it`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 1), defeatUnits(unitId = "knight", count = 1))))
        apis.startedPresenter()
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
    fun `QuestPresenter should announce the completion once when the last defeat publishes three events`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 1))))
        apis.startedPresenter()
        // When
        apis.defeatRat()
        // Then
        trackerView.displayedQuests.size shouldBe 2
        trackerView.displayedQuests
            .last()
            .isCompleted
            .shouldBeTrue()
        trackerView.pops shouldBe 0
        announcementView.announcements.map { (moment, _) -> moment }.shouldContainExactly(Moment.STARTED, Moment.COMPLETED)
        announcementView.announcements
            .last()
            .second.gold shouldBe 150
    }

    @Test
    fun `QuestPresenter should draw the tracker without announcing when a game is restored`() {
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
        announcementView.announcements.shouldBeEmpty()
        timeline.isPlaying.shouldBeFalse()
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

    @Test
    fun `QuestPresenter should close the detail view when a player wins`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        val presenter = apis.presenter()
        presenter.openQuest()
        // When
        apis.eventBus.publish(PlayerVictory(playerId = "human"))
        apis.eventBus.dispatch()
        // Then
        presenter.isQuestOpen.shouldBeFalse()
        detailView.visible.shouldBeFalse()
    }

    @Test
    fun `OpenQuest should display the quest in its completed state when it is completed`() {
        // Given
        val apis = apis(mainQuest(id = questId, tasks = listOf(defeatUnits(unitId = "rat", count = 1))))
        val presenter = apis.startedPresenter()
        apis.defeatRat()
        // When
        presenter.openQuest()
        // Then
        presenter.isQuestOpen.shouldBeTrue()
        detailView.displayedCaption() shouldBe "QUEST COMPLETED"
    }

    @Test
    fun `QuestDetailBackSelected should close the quest when the detail view is open`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        val presenter = apis.presenter()
        apis.partyApi.openQuest()
        apis.eventBus.dispatch()
        presenter.openQuest()
        // When
        presenter.questDetailBackSelected()
        // Then
        apis.partyApi
            .searchParty()!!
            .isReadingQuest
            .shouldBeFalse()
    }

    @Test
    fun `QuestDetailBackSelected should do nothing when the timeline is playing`() {
        // Given
        val apis = apis(mainQuest(id = questId))
        val presenter = apis.presenter()
        apis.partyApi.openQuest()
        apis.eventBus.dispatch()
        presenter.openQuest()
        timeline.playLongStep()
        // When
        presenter.questDetailBackSelected()
        // Then
        apis.partyApi
            .searchParty()!!
            .isReadingQuest
            .shouldBeTrue()
    }

    // The main quest under way and the side quests of the farmer started, with the party reading them.
    private fun SubdomainApis.presenterWithASideQuestOpen(): QuestPresenter {
        val presenter = presenter()
        startQuest()
        questApi.resolveNpcTalk("farmer")
        partyApi.openQuest()
        eventBus.dispatch()
        presenter.openQuest()
        return presenter
    }

    @Test
    fun `OpenQuest should open the journal when a side quest is listed`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        // When
        apis.presenterWithASideQuestOpen()
        // Then
        journalView.visible.shouldBeTrue()
        detailView.visible.shouldBeFalse()
        journalView.displayedRows().shouldContainExactly(questId, "side")
    }

    @Test
    fun `OpenQuest should open the detail view when only the main quest is listed`() {
        // Given: a side quest that has not started is hidden.
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        val presenter = apis.presenter()
        apis.startQuest()
        // When
        presenter.openQuest()
        // Then
        detailView.visible.shouldBeTrue()
        journalView.visible.shouldBeFalse()
    }

    @Test
    fun `OpenQuest should list the active quests before the completed ones when one is completed`() {
        // Given
        val completed = sideQuest(id = "completed", tasks = listOf(defeatUnits(unitId = "rat", count = 1)))
        val active = sideQuest(id = "active")
        val apis = apis(completed, mainQuest(id = questId), active)
        val presenter = apis.presenter()
        apis.startQuest()
        apis.questApi.resolveNpcTalk("farmer")
        apis.eventBus.dispatch()
        apis.defeatRat()
        // When
        presenter.openQuest()
        // Then
        journalView.displayedRows().shouldContainExactly(questId, "active", "completed")
    }

    @Test
    fun `QuestJournalSelected should display the quest in full in place of the journal`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        val presenter = apis.presenterWithASideQuestOpen()
        // When
        presenter.questJournalSelected("side")
        // Then
        detailView.visible.shouldBeTrue()
        journalView.visible.shouldBeFalse()
        detailView.displayedCaption() shouldBe "SIDE QUEST"
    }

    @Test
    fun `QuestDetailBackSelected should display the journal again when it was opened from it`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        val presenter = apis.presenterWithASideQuestOpen()
        presenter.questJournalSelected("side")
        // When
        presenter.questDetailBackSelected()
        apis.eventBus.dispatch()
        // Then
        journalView.visible.shouldBeTrue()
        detailView.visible.shouldBeFalse()
        apis.partyApi
            .searchParty()!!
            .isReadingQuest
            .shouldBeTrue()
    }

    @Test
    fun `QuestJournalBackSelected should close the quest when the journal is open`() {
        // Given
        val apis = apis(mainQuest(id = questId), sideQuest(id = "side"))
        val presenter = apis.presenterWithASideQuestOpen()
        // When
        presenter.questJournalBackSelected()
        // Then
        apis.partyApi
            .searchParty()!!
            .isReadingQuest
            .shouldBeFalse()
    }
}
