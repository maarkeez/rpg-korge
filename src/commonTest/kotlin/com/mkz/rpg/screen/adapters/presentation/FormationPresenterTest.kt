package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FormationPresenterTest {
    // The knight leads from (1,1), followed by the viking at (2,1) and the herbalist at (2,2). There is no battle.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight").toDto(), unit(id = "viking").toDto(), unit(id = "herbalist").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(3) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "viking-1", unitId = "viking", playerId = "human", row = 2, column = 1),
                    Deployment(battleUnitId = "herbalist-1", unitId = "herbalist", playerId = "human", row = 2, column = 2),
                ),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val formationView = FormationView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val battlefieldView = FakeBattlefieldView()
    private val timeline = battleSceneTimeline()
    private val presenter = FormationPresenter(formationView, battlefieldView, apis.partyApi, apis.battleUnitApi, apis.battlefieldApi, TextsMother.texts(), timeline)

    private val knightTile = TilePosition(row = 1, column = 1)
    private val vikingTile = TilePosition(row = 2, column = 1)
    private val herbalistTile = TilePosition(row = 2, column = 2)

    // The party explores, and the formation opens, as when B is pressed.
    private fun openTheFormation() {
        apis.partyApi.startExploration()
        eventBus.dispatch()
        apis.partyApi.openFormation()
        eventBus.dispatch()
        timeline.play { presenter.open() }
    }

    @Test
    fun `Init should hide the formation when is created`() {
        // Then
        formationView.visible.shouldBeFalse()
        presenter.isOpen.shouldBeFalse()
    }

    @Test
    fun `Open should display the party in order, the leader crowned, and dim every tile but the party's`() {
        // When
        openTheFormation()
        // Then
        presenter.isOpen.shouldBeTrue()
        formationView.visible.shouldBeTrue()
        formationView.names().shouldContainExactly("Knight", "Viking", "Herbalist")
        formationView.crownedRowIndexes().shouldContainExactly(0)
        formationView.selectedRowIndexes().shouldBeEmpty()
        formationView.hint() shouldBe "Tap two members to swap them"
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile, vikingTile, herbalistTile)
    }

    @Test
    fun `FormationRowSelected should select the member, framing its row and its tile`() {
        // Given
        openTheFormation()
        // When
        presenter.formationRowSelected(2)
        // Then
        formationView.selectedRowIndexes().shouldContainExactly(2)
        formationView.hint() shouldBe "Swap Herbalist with..."
        battlefieldView.tileSelections.last() shouldBe herbalistTile
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile, vikingTile, herbalistTile)
    }

    @Test
    fun `FormationRowSelected should swap both members and clear the selection when another member is tapped`() {
        // Given
        openTheFormation()
        presenter.formationRowSelected(2)
        // When
        presenter.formationRowSelected(0)
        // Then
        formationView.names().shouldContainExactly("Herbalist", "Viking", "Knight")
        formationView.crownedRowIndexes().shouldContainExactly(0)
        formationView.selectedRowIndexes().shouldBeEmpty()
        formationView.hint() shouldBe "Tap two members to swap them"
    }

    @Test
    fun `FormationRowSelected should clear the selection when the selected member is tapped again`() {
        // Given
        openTheFormation()
        presenter.formationRowSelected(1)
        val resetTilesCount = battlefieldView.resetTilesCount
        // When
        presenter.formationRowSelected(1)
        // Then
        formationView.names().shouldContainExactly("Knight", "Viking", "Herbalist")
        formationView.selectedRowIndexes().shouldBeEmpty()
        battlefieldView.resetTilesCount shouldBe resetTilesCount + 1
    }

    @Test
    fun `FormationRowSelected should do nothing while the scene plays the previous steps`() {
        // Given
        openTheFormation()
        timeline.playLongStep()
        // When
        presenter.formationRowSelected(1)
        // Then
        formationView.selectedRowIndexes().shouldBeEmpty()
    }

    @Test
    fun `FormationCloseSelected should apply the order displayed to the party, the first member leading`() {
        // Given
        openTheFormation()
        presenter.formationRowSelected(2)
        presenter.formationRowSelected(0)
        val closed = mutableListOf<PartyEvent.FormationClosed>()
        eventBus.subscribe<PartyEvent.FormationClosed> { event -> closed += event }
        // When
        presenter.formationCloseSelected()
        eventBus.dispatch()
        // Then
        val party = apis.partyApi.searchParty()!!
        party.leaderBattleUnitId shouldBe "herbalist-1"
        party.battleUnitIds.shouldContainExactly("herbalist-1", "viking-1", "knight-1")
        party.isExploring.shouldBeTrue()
        closed.shouldContainExactly(PartyEvent.FormationClosed(playerId = "human", battleUnitIds = listOf("herbalist-1", "viking-1", "knight-1")))
    }

    @Test
    fun `Close should hide the formation and restore the tiles`() {
        // Given
        openTheFormation()
        val resetTilesCount = battlefieldView.resetTilesCount
        // When
        presenter.close()
        // Then
        presenter.isOpen.shouldBeFalse()
        formationView.visible.shouldBeFalse()
        battlefieldView.resetTilesCount shouldBe resetTilesCount + 1
    }
}
