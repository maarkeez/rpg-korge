package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.mapBuilder.adapters.presentation.MapBuilderApi
import com.mkz.rpg.screen.adapters.presentation.FakeMapBuilderCallToActionView.Actions
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test

class MapBuilderPresenterTest {
    // As in the game, only sand has transitions: to lava and to water.
    private val apis =
        SubdomainApis(
            terrains =
                listOf(
                    occupiableTerrain(id = "sand", allowedTransitionTo = setOf("lava", "water")).toDto(),
                    nonOccupiableTerrain(id = "lava").toDto(),
                    occupiableTerrain(id = "water").toDto(),
                ),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val mapBuilderApi = MapBuilderApi(apis.terrainApi, apis.battlefieldApi, eventBus)
    private val battlefieldView = FakeBattlefieldView()
    private val infoView = FakeMapBuilderInfoView()
    private val hudView = FakeMapBuilderHudView()
    private val callToActionView = FakeMapBuilderCallToActionView()
    private val clipboard = FakeClipboard()
    private var leftCount = 0

    init {
        MapBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            mapBuilderApi = mapBuilderApi,
            battlefieldApi = apis.battlefieldApi,
            terrainApi = apis.terrainApi,
            clipboard = clipboard,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            onLeft = { leftCount++ },
            eventBus = eventBus,
        )
    }

    private fun givenMapCreated() {
        mapBuilderApi.createMap()
        eventBus.dispatch()
    }

    private fun selectTile(
        row: Int,
        column: Int,
    ) {
        battlefieldView.delegate!!.tileSelected(row, column)
        eventBus.dispatch()
    }

    private fun selectTerrain(terrainId: String) {
        hudView.delegate!!.terrainSelected(terrainId)
        eventBus.dispatch()
    }

    private fun exportAndBack() = callToActionView.displayedActions.shouldBeInstanceOf<Actions.ExportAndBack>()

    @Test
    fun `MapCreated should display the map on the battlefield as the battle would when the map is created`() {
        // When
        givenMapCreated()
        // Then
        battlefieldView.displayedBattlefields.shouldContainExactly(apis.battlefieldApi.previewBattlefield(List(16) { List(16) { "sand" } }))
        infoView.displayedSize shouldBe (16 to 16)
        hudView.displayedSize shouldBe (16 to 16)
    }

    @Test
    fun `MapCreated should display every terrain with its tile image and sand selected when the map is created`() {
        // When
        givenMapCreated()
        // Then
        hudView.displayedTerrains shouldBe
            listOf("lava", "sand", "water").map { terrainId -> MapBuilderHudView.TerrainOption(terrainId, battlefieldView.terrainTileBitmap(terrainId, null)) }
        hudView.selectedTerrainId shouldBe "sand"
    }

    @Test
    fun `MapCreated should display exporting and going back when the map builder is displayed`() {
        // Then
        exportAndBack()
    }

    @Test
    fun `TerrainSelected should highlight the terrain when a terrain is selected in the palette`() {
        // Given
        givenMapCreated()
        // When
        selectTerrain("lava")
        // Then
        hudView.selectedTerrainId shouldBe "lava"
    }

    @Test
    fun `TilePainted should draw again the painted tile and its neighbours when a tile is selected`() {
        // Given
        givenMapCreated()
        selectTerrain("lava")
        // When
        selectTile(row = 2, column = 3)
        // Then
        val updatedTerrain = battlefieldView.updatedTerrains.single()
        updatedTerrain.battlefield.tiles
            .getValue(PositionDto(2, 3))
            .terrainId shouldBe "lava"
        updatedTerrain.positions shouldBe setOf(PositionDto(2, 3), PositionDto(1, 3), PositionDto(2, 4), PositionDto(3, 3), PositionDto(2, 2))
    }

    @Test
    fun `TilePainted should only draw again the neighbours inside the map when a corner tile is selected`() {
        // Given
        givenMapCreated()
        selectTerrain("lava")
        // When
        selectTile(row = 0, column = 0)
        // Then
        battlefieldView.updatedTerrains.single().positions shouldBe setOf(PositionDto(0, 0), PositionDto(0, 1), PositionDto(1, 0))
    }

    @Test
    fun `TilePaintRejected should display why and keep the tile when the terrain can not be next to a neighbour`() {
        // Given
        givenMapCreated()
        selectTerrain("lava")
        selectTile(row = 2, column = 3)
        selectTerrain("water")
        // When
        selectTile(row = 2, column = 4)
        // Then
        infoView.errors.shouldContainExactly("Water can not be next to lava")
        battlefieldView.updatedTerrains.shouldHaveSize(1)
    }

    @Test
    fun `SizeChanged should display the resized map when the size is changed`() {
        // Given
        givenMapCreated()
        // When
        hudView.delegate!!.sizeChanged(rows = 24, columns = 8)
        eventBus.dispatch()
        // Then
        battlefieldView.displayedBattlefields.last() shouldBe apis.battlefieldApi.previewBattlefield(List(24) { List(8) { "sand" } })
        infoView.displayedSize shouldBe (24 to 8)
        hudView.displayedSize shouldBe (24 to 8)
    }

    @Test
    fun `Export should copy the map to the clipboard and tell it when the map is exported`() {
        // Given
        givenMapCreated()
        selectTerrain("lava")
        selectTile(row = 0, column = 0)
        // When
        exportAndBack().onExported()
        // Then
        clipboard.copiedTexts.shouldContainExactly(mapBuilderApi.exportMap())
        infoView.infos.shouldContainExactly(MapBuilderPresenter.MAP_COPIED)
    }

    @Test
    fun `Back should ask to confirm leaving and warn the map is lost when back is selected`() {
        // Given
        givenMapCreated()
        // When
        exportAndBack().onBack()
        // Then
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>()
        infoView.infos.shouldContainExactly(MapBuilderPresenter.MAP_LOST_WHEN_LEAVING)
        leftCount shouldBe 0
    }

    @Test
    fun `Stay should display exporting and going back again when staying is selected`() {
        // Given
        givenMapCreated()
        exportAndBack().onBack()
        // When
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>().onStayed()
        // Then
        exportAndBack()
        leftCount shouldBe 0
    }

    @Test
    fun `Leave should leave the map builder when leaving is confirmed`() {
        // Given
        givenMapCreated()
        exportAndBack().onBack()
        // When
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>().onLeft()
        // Then
        leftCount shouldBe 1
    }

    @Test
    fun `TileSelected should not paint when the tile already has the selected terrain`() {
        // Given
        givenMapCreated()
        // When
        selectTile(row = 0, column = 0)
        // Then
        battlefieldView.updatedTerrains.shouldBeEmpty()
        infoView.errors.shouldBeEmpty()
    }
}
