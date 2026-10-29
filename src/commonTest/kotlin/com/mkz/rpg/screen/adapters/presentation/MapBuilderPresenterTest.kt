package com.mkz.rpg.screen.adapters.presentation

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
    private val menuView = FakeMapBuilderMenuView()
    private val clipboard = FakeClipboard()
    private val mapFilePicker = FakeMapFilePicker()
    private var leftCount = 0

    init {
        MapBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            menuView = menuView,
            mapBuilderApi = mapBuilderApi,
            battlefieldApi = apis.battlefieldApi,
            terrainApi = apis.terrainApi,
            texts = TextsMother.texts(),
            clipboard = clipboard,
            mapFilePicker = mapFilePicker,
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
        battlefieldView.displayedBattlefields.shouldContainExactly(BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(List(16) { List(16) { "sand" } })))
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
            .getValue(TilePosition(2, 3))
            .terrainId shouldBe "lava"
        updatedTerrain.positions shouldBe setOf(TilePosition(2, 3), TilePosition(1, 3), TilePosition(2, 4), TilePosition(3, 3), TilePosition(2, 2))
    }

    @Test
    fun `TilePainted should only draw again the neighbours inside the map when a corner tile is selected`() {
        // Given
        givenMapCreated()
        selectTerrain("lava")
        // When
        selectTile(row = 0, column = 0)
        // Then
        battlefieldView.updatedTerrains.single().positions shouldBe setOf(TilePosition(0, 0), TilePosition(0, 1), TilePosition(1, 0))
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
        infoView.errors.shouldContainExactly("Water can not be next to Lava")
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
        battlefieldView.displayedBattlefields.last() shouldBe BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(List(24) { List(8) { "sand" } }))
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
        infoView.infos.shouldContainExactly("Map copied to the clipboard")
    }

    @Test
    fun `Back should ask to confirm leaving and warn the map is lost when back is selected`() {
        // Given
        givenMapCreated()
        // When
        exportAndBack().onBack()
        // Then
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>()
        infoView.infos.shouldContainExactly("The map is lost when leaving")
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

    private fun importFile(text: String?) {
        mapFilePicker.pickedText = text
        menuView.select("Import")
        eventBus.dispatch()
    }

    @Test
    fun `MapBuilderMenuView should offer importing a map when the map builder is displayed`() {
        // Then
        menuView.displayedOptions.map(MapBuilderMenuView.Option::label).shouldContainExactly("Import")
    }

    @Test
    fun `Import should display the map in the file and tell it when a map is imported`() {
        // Given
        givenMapCreated()
        val tiles = listOf(listOf("sand", "lava", "sand"), listOf("sand", "sand", "water"))
        // When
        importFile(apis.battlefieldApi.exportBattlefield(tiles))
        // Then
        battlefieldView.displayedBattlefields.last() shouldBe BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(tiles))
        infoView.displayedSize shouldBe (2 to 3)
        hudView.displayedSize shouldBe (2 to 3)
        infoView.infos.shouldContainExactly("Map imported")
    }

    @Test
    fun `Import should keep the map and display nothing when no file is picked`() {
        // Given
        givenMapCreated()
        // When
        importFile(null)
        // Then
        infoView.errors.shouldBeEmpty()
        infoView.infos.shouldBeEmpty()
        battlefieldView.displayedBattlefields.shouldHaveSize(1)
    }

    @Test
    fun `Import should display why and keep the map when the file can not be read`() {
        // Given
        givenMapCreated()
        mapFilePicker.failure = IllegalStateException("The file was removed")
        // When
        importFile("Never read")
        // Then
        infoView.errors.shouldContainExactly("The file can not be read")
        battlefieldView.displayedBattlefields.shouldHaveSize(1)
    }

    @Test
    fun `Import should display why when the file has no map`() {
        // Given
        givenMapCreated()
        // When
        importFile("Hello, map builder!")
        // Then
        infoView.errors.shouldContainExactly("The file has no map")
    }

    @Test
    fun `Import should display why when the file is empty`() {
        // Given
        givenMapCreated()
        // When
        importFile("")
        // Then
        infoView.errors.shouldContainExactly("The file has no map")
    }

    @Test
    fun `Import should display why when the map in the file is larger than allowed`() {
        // Given
        givenMapCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(List(129) { List(2) { "sand" } }))
        // Then
        infoView.errors.shouldContainExactly("The map must be 1 to 128 tiles wide and tall")
    }

    @Test
    fun `Import should display the terrain when the map in the file has an unknown terrain`() {
        // Given
        givenMapCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(listOf(listOf("sand", "forest"))))
        // Then
        infoView.errors.shouldContainExactly("Unknown terrain: forest")
    }

    @Test
    fun `Import should display the terrains when two neighbours of the map in the file can not be next to each other`() {
        // Given
        givenMapCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(listOf(listOf("water", "lava"))))
        // Then
        infoView.errors.shouldContainExactly("Water can not be next to Lava")
    }
}
