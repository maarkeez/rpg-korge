package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.math.geom.Size
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ExplorationPresenterTest {
    // The knight explores the sand from (1,1), next to The Guy, who stands west of it at (1,0). There is no battle yet.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(3) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1)),
            npcs = listOf(npc(id = "the-guy", name = "The Guy", line = "G'day bloke!", row = 1, column = 0).toDto()),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val virtualPadView = VirtualPadView()
    private val npcDialogView = NpcDialogView(Size(390, 300), BattleSceneResourcesLoader())
    private val battleHudView = FakeBattleHudView()
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()

    private fun createPresenter() = ExplorationPresenter(virtualPadView, npcDialogView, battleHudView, playerCallToActionView, apis.explorationApi, apis.npcApi, timeline, eventBus)

    private fun startExploring() {
        apis.explorationApi.startExploration()
        eventBus.dispatch()
    }

    @Test
    fun `Init should hide the virtual pad when is created and the exploration has not started yet`() {
        // When
        createPresenter()
        // Then
        virtualPadView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should display the virtual pad in place of the battle HUD and the call to action when the exploration starts`() {
        // Given
        createPresenter()
        // When
        startExploring()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should display the battle HUD and the call to action in place of the virtual pad when the exploration pauses`() {
        // Given
        createPresenter()
        startExploring()
        // When
        eventBus.publish(ExplorationEvent.ExplorationPaused(playerId = "human"))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayExploring should display the virtual pad in place of the battle HUD and the call to action when the exploration resumes`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(ExplorationEvent.ExplorationPaused(playerId = "human"))
        eventBus.dispatch()
        // When
        eventBus.publish(ExplorationEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1")))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `WalkParty should walk the party towards the held arrow when the scene is idle`() {
        // Given
        createPresenter()
        startExploring()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
    }

    @Test
    fun `WalkParty should not walk the party while the scene plays the previous steps`() {
        // Given
        createPresenter()
        startExploring()
        timeline.playLongStep()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    private fun requestedTerrainEffects() =
        mutableListOf<Set<String>>().also { requests ->
            eventBus.subscribe<BattlefieldEvent.RequestEffectApplicationToOccupants> { requests += it.battleUnitIds }
        }

    @Test
    fun `Tick should apply the terrain effects of the battle units standing still every second while exploring`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(999.milliseconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
        // When
        presenter.tick(1.milliseconds)
        presenter.tick(2.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldContainExactly(setOf("knight-1"), setOf("knight-1"), setOf("knight-1"))
    }

    @Test
    fun `Tick should no longer apply the terrain effects and the virtual pad should be hidden when the game is over`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val requests = requestedTerrainEffects()
        eventBus.publish(PlayerVictory(playerId = "cpu"))
        eventBus.dispatch()
        // When
        presenter.tick(1.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
        virtualPadView.visible.shouldBeFalse()
    }

    private val knightSelected = BattlefieldHudEvent.SelectedBattleUnit(tile = TileDto(row = 1, column = 1), battleUnitId = "knight-1", tilesWhereCanBeMoved = emptySet())

    @Test
    fun `DisplayVirtualPad should display the battle HUD and the call to action in place of the pad when a battle unit is selected while exploring`() {
        // Given
        createPresenter()
        startExploring()
        // When
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayVirtualPad should display the pad again when the battlefield HUD goes idle while exploring`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // When
        eventBus.publish(BattlefieldHudEvent.Idle)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayVirtualPad should keep the battle HUD when the battlefield HUD goes idle while battling`() {
        // Given
        createPresenter()
        startExploring()
        apis.explorationApi.pauseExploration()
        eventBus.dispatch()
        // When
        eventBus.publish(BattlefieldHudEvent.Idle)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayVirtualPad should display the battle HUD straight away when a found item does not fit in the inventory while exploring`() {
        // Given
        createPresenter()
        startExploring()
        timeline.playLongStep()
        // When
        eventBus.publish(InventoryEvent.InventoryFull(battleUnitId = "knight-1", lootableItemId = "hammer", row = 1, column = 1))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `WalkParty should not walk the party while the pad is hidden, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    private fun talk() {
        virtualPadView.pressAButton()
        eventBus.dispatch()
    }

    @Test
    fun `TalkToAdjacentNpc should display the dialog of the npc next to the leader in place of the pad when A is pressed`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talk()
        // Then
        npcDialogView.visible.shouldBeTrue()
        npcDialogView.displayedName() shouldBe "The Guy"
        npcDialogView.displayedLine() shouldBe "G'day bloke!"
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `TalkToAdjacentNpc should not talk while the scene plays the previous steps`() {
        // Given
        createPresenter()
        startExploring()
        timeline.playLongStep()
        // When
        talk()
        // Then
        apis.explorationApi.searchExploration()!!.talkingNpcId shouldBe null
    }

    @Test
    fun `TalkToAdjacentNpc should do nothing when no npc is next to the leader`() {
        // Given
        createPresenter()
        startExploring()
        virtualPadView.holdArrow(DirectionDto.NORTH)
        virtualPadView.releaseArrow()
        eventBus.dispatch()
        // When
        talk()
        // Then
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
    }

    @Test
    fun `Tick should not apply the terrain effects while talking`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        talk()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(3.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
    }

    @Test
    fun `WalkParty should not walk the party while talking, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    @Test
    fun `FinishTalk should display the pad again and explore when the dialog is tapped`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        npcDialogView.tapDialog()
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        apis.explorationApi
            .searchExploration()!!
            .isExploring
            .shouldBeTrue()
    }

    @Test
    fun `DisplayExploring should close the dialog when the exploration pauses while talking`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        eventBus.publish(ExplorationEvent.ExplorationPaused(playerId = "human"))
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
    }
}
