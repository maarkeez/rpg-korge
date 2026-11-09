package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.FakeWorldBuilderCallToActionView.Actions
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.worldBuilder.adapters.presentation.WorldBuilderApi
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test

class WorldBuilderPresenterTest {
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
    private val worldBuilderApi = WorldBuilderApi(apis.terrainApi, apis.battlefieldApi, eventBus)
    private val battlefieldView = FakeBattlefieldView()
    private val infoView = FakeWorldBuilderInfoView()
    private val hudView = FakeWorldBuilderHudView()
    private val callToActionView = FakeWorldBuilderCallToActionView()
    private val menuView = FakeWorldBuilderMenuView()
    private val clipboard = FakeClipboard()
    private val worldFilePicker = FakeWorldFilePicker()
    private var leftCount = 0

    init {
        WorldBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            menuView = menuView,
            worldBuilderApi = worldBuilderApi,
            battlefieldApi = apis.battlefieldApi,
            terrainApi = apis.terrainApi,
            messages = MessagesMother.messages(),
            clipboard = clipboard,
            worldFilePicker = worldFilePicker,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            onLeft = { leftCount++ },
            eventBus = eventBus,
        )
    }

    private fun givenWorldCreated() {
        worldBuilderApi.createWorld()
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
    fun `WorldCreated should display the world on the battlefield as the battle would when the world is created`() {
        // When
        givenWorldCreated()
        // Then
        battlefieldView.displayedBattlefields.shouldContainExactly(BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(List(16) { List(16) { "sand" } })))
        infoView.displayedSize shouldBe (16 to 16)
        hudView.displayedSize shouldBe (16 to 16)
        hudView.displayedAllowedSizes shouldBe (1..128)
    }

    @Test
    fun `WorldCreated should display every terrain with its tile image and sand selected when the world is created`() {
        // When
        givenWorldCreated()
        // Then
        hudView.displayedTerrains shouldBe
            listOf("lava", "sand", "water").map { terrainId -> WorldBuilderHudView.TerrainOption(terrainId, battlefieldView.terrainTileBitmap(terrainId, null)) }
        hudView.selectedTerrainId shouldBe "sand"
    }

    @Test
    fun `WorldCreated should display exporting and going back when the world builder is displayed`() {
        // Then
        exportAndBack()
    }

    @Test
    fun `TerrainSelected should highlight the terrain when a terrain is selected in the palette`() {
        // Given
        givenWorldCreated()
        // When
        selectTerrain("lava")
        // Then
        hudView.selectedTerrainId shouldBe "lava"
    }

    @Test
    fun `TilePainted should draw again the painted tile and its adjacent tiles when a tile is selected`() {
        // Given
        givenWorldCreated()
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
    fun `TilePainted should only draw again the adjacent tiles inside the world when a corner tile is selected`() {
        // Given
        givenWorldCreated()
        selectTerrain("lava")
        // When
        selectTile(row = 0, column = 0)
        // Then
        battlefieldView.updatedTerrains.single().positions shouldBe setOf(TilePosition(0, 0), TilePosition(0, 1), TilePosition(1, 0))
    }

    @Test
    fun `TilePaintRejected should display why and keep the tile when the terrain can not be next to an adjacent tile`() {
        // Given
        givenWorldCreated()
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
    fun `SizeChanged should display the resized world when the size is changed`() {
        // Given
        givenWorldCreated()
        // When
        hudView.delegate!!.sizeChanged(rows = 24, columns = 8)
        eventBus.dispatch()
        // Then
        battlefieldView.displayedBattlefields.last() shouldBe BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(List(24) { List(8) { "sand" } }))
        infoView.displayedSize shouldBe (24 to 8)
        hudView.displayedSize shouldBe (24 to 8)
    }

    @Test
    fun `Export should copy the world to the clipboard and tell it when the world is exported`() {
        // Given
        givenWorldCreated()
        selectTerrain("lava")
        selectTile(row = 0, column = 0)
        // When
        exportAndBack().onExported()
        // Then
        clipboard.copiedTexts.shouldContainExactly(worldBuilderApi.exportWorld())
        infoView.infos.shouldContainExactly("World copied to the clipboard")
    }

    @Test
    fun `Back should ask to confirm leaving and warn the world is lost when back is selected`() {
        // Given
        givenWorldCreated()
        // When
        exportAndBack().onBack()
        // Then
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>()
        infoView.infos.shouldContainExactly("The world is lost when leaving")
        leftCount shouldBe 0
    }

    @Test
    fun `Stay should display exporting and going back again when staying is selected`() {
        // Given
        givenWorldCreated()
        exportAndBack().onBack()
        // When
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>().onStayed()
        // Then
        exportAndBack()
        leftCount shouldBe 0
    }

    @Test
    fun `Leave should leave the world builder when leaving is confirmed`() {
        // Given
        givenWorldCreated()
        exportAndBack().onBack()
        // When
        callToActionView.displayedActions.shouldBeInstanceOf<Actions.LeaveConfirmation>().onLeft()
        // Then
        leftCount shouldBe 1
    }

    @Test
    fun `TileSelected should not paint when the tile already has the selected terrain`() {
        // Given
        givenWorldCreated()
        // When
        selectTile(row = 0, column = 0)
        // Then
        battlefieldView.updatedTerrains.shouldBeEmpty()
        infoView.errors.shouldBeEmpty()
    }

    private fun importFile(text: String?) {
        worldFilePicker.pickedText = text
        menuView.select("Import")
        eventBus.dispatch()
    }

    @Test
    fun `WorldBuilderMenuView should offer importing a world when the world builder is displayed`() {
        // Then
        menuView.displayedOptions.map(WorldBuilderMenuView.Option::label).shouldContainExactly("Import")
    }

    @Test
    fun `Import should display the world in the file and tell it when a world is imported`() {
        // Given
        givenWorldCreated()
        val tiles = listOf(listOf("sand", "lava", "sand"), listOf("sand", "sand", "water"))
        // When
        importFile(apis.battlefieldApi.exportBattlefield(tiles))
        // Then
        battlefieldView.displayedBattlefields.last() shouldBe BattlefieldMapper.terrain(apis.battlefieldApi.previewBattlefield(tiles))
        infoView.displayedSize shouldBe (2 to 3)
        hudView.displayedSize shouldBe (2 to 3)
        infoView.infos.shouldContainExactly("World imported")
    }

    @Test
    fun `Import should keep the world and display nothing when no file is picked`() {
        // Given
        givenWorldCreated()
        // When
        importFile(null)
        // Then
        infoView.errors.shouldBeEmpty()
        infoView.infos.shouldBeEmpty()
        battlefieldView.displayedBattlefields.shouldHaveSize(1)
    }

    @Test
    fun `Import should display why and keep the world when the file can not be read`() {
        // Given
        givenWorldCreated()
        worldFilePicker.failure = IllegalStateException("The file was removed")
        // When
        importFile("Never read")
        // Then
        infoView.errors.shouldContainExactly("The file can not be read")
        battlefieldView.displayedBattlefields.shouldHaveSize(1)
    }

    @Test
    fun `Import should display why when the file has no world`() {
        // Given
        givenWorldCreated()
        // When
        importFile("Hello, world builder!")
        // Then
        infoView.errors.shouldContainExactly("The file has no world")
    }

    @Test
    fun `Import should display why when the file is empty`() {
        // Given
        givenWorldCreated()
        // When
        importFile("")
        // Then
        infoView.errors.shouldContainExactly("The file has no world")
    }

    @Test
    fun `Import should display why when the world in the file is larger than allowed`() {
        // Given
        givenWorldCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(List(129) { List(2) { "sand" } }))
        // Then
        infoView.errors.shouldContainExactly("The world must be 1 to 128 tiles wide and tall")
    }

    @Test
    fun `Import should display the terrain when the world in the file has an unknown terrain`() {
        // Given
        givenWorldCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(listOf(listOf("sand", "forest"))))
        // Then
        infoView.errors.shouldContainExactly("Unknown terrain: forest")
    }

    @Test
    fun `Import should display the terrains when two adjacent tiles of the world in the file can not be next to each other`() {
        // Given
        givenWorldCreated()
        // When
        importFile(apis.battlefieldApi.exportBattlefield(listOf(listOf("water", "lava"))))
        // Then
        infoView.errors.shouldContainExactly("Water can not be next to Lava")
    }
}
