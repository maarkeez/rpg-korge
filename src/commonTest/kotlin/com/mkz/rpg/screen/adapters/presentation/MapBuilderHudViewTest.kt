package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MapBuilderHudViewTest : ViewsForTesting() {
    private val sizes = mutableListOf<Pair<Int, Int>>()
    private val selectedTerrains = mutableListOf<String>()
    private val delegate =
        object : MapBuilderHudView.Delegate {
            override fun sizeChanged(
                rows: Int,
                columns: Int,
            ) {
                sizes += rows to columns
            }

            override fun terrainSelected(terrainId: String) {
                selectedTerrains += terrainId
            }
        }

    private val sandBitmap = Bitmap32(16, 16, Colors.YELLOW)
    private val lavaBitmap = Bitmap32(16, 16, Colors.RED)
    private val terrains = listOf(MapBuilderHudView.TerrainOption("lava", lavaBitmap), MapBuilderHudView.TerrainOption("sand", sandBitmap))

    private fun hudView() = MapBuilderHudView().also { hudView -> hudView.setDelegate(delegate) }

    @Test
    fun `DisplaySize should display the rows and the columns when the size is displayed`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            // When
            hudView.displaySize(rows = 12, columns = 40)
            // Then
            hudView.rowsText() shouldBe "12"
            hudView.columnsText() shouldBe "40"
        }

    @Test
    fun `DisplaySize should disable the steps out of 1 to 32 when the size is close to the limits`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            // When
            hudView.displaySize(rows = 5, columns = 28)
            // Then
            hudView.enabledRowsSteps().shouldContainExactly(-1, 1, 8)
            hudView.enabledColumnsSteps().shouldContainExactly(-8, -1, 1)
        }

    @Test
    fun `RowsStep should ask for the size with the rows stepped when a rows step is clicked`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            hudView.displaySize(rows = 16, columns = 20)
            addChild(hudView)
            // When
            clickRowsStep(hudView, step = 8)
            // Then
            sizes.shouldContainExactly(24 to 20)
        }

    @Test
    fun `ColumnsStep should ask for the size with the columns stepped when a columns step is clicked`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            hudView.displaySize(rows = 16, columns = 20)
            addChild(hudView)
            // When
            clickColumnsStep(hudView, step = -1)
            // Then
            sizes.shouldContainExactly(16 to 19)
        }

    @Test
    fun `DisplayTerrains should display each terrain tile and highlight the selected one when the terrains are displayed`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            // When
            hudView.displayTerrains(terrains, selectedTerrainId = "sand")
            // Then
            hudView.paletteTerrainIds().shouldContainExactly("lava", "sand")
            hudView.paletteBitmap("lava") shouldBe lavaBitmap
            hudView.highlightedTerrainId() shouldBe "sand"
        }

    @Test
    fun `SelectTerrain should highlight only the selected terrain when another terrain is selected`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            hudView.displayTerrains(terrains, selectedTerrainId = "sand")
            // When
            hudView.selectTerrain("lava")
            // Then
            hudView.highlightedTerrainId() shouldBe "lava"
        }

    @Test
    fun `PaletteTerrain should ask to select the terrain when it is clicked`() =
        sharedViewsTest {
            // Given
            val hudView = hudView()
            hudView.displayTerrains(terrains, selectedTerrainId = "sand")
            addChild(hudView)
            // When
            clickPaletteTerrain(hudView, "lava")
            // Then
            selectedTerrains.shouldContainExactly("lava")
        }
}
