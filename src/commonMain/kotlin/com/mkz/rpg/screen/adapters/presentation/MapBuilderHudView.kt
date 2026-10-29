package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/** The map builder controls: the map size, and the terrain the tiles are painted with. */
open class MapBuilderHudView(
    texts: Texts,
) : UIContainer(Size(width = 390, height = 300)) {
    companion object {
        // The steps a size stepper changes the size by, so a large map takes a few taps.
        val SIZE_STEPS = listOf(-8, -1, 1, 8)
        const val STEP_BUTTON_WIDTH = 44.0
        const val LABEL_WIDTH = 80.0
        const val VALUE_WIDTH = 50.0

        const val TERRAIN_BUTTON_SIZE = 48.0
        const val TERRAIN_SCALE = 3.0
        const val SELECTION_BORDER = 3.0
        const val TERRAIN_IMAGE = "TERRAIN_IMAGE"
    }

    data class TerrainOption(
        val terrainId: String,
        val bitmap: Bitmap,
    )

    interface Delegate {
        fun sizeChanged(
            rows: Int,
            columns: Int,
        )

        fun terrainSelected(terrainId: String)
    }

    private var delegate: Delegate? = null
    private var rows = 0
    private var columns = 0
    private var allowedSizes = IntRange.EMPTY

    internal val rowsValue: UIText
    internal val columnsValue: UIText
    internal val rowsStepButtons: Map<Int, UIButton>
    internal val columnsStepButtons: Map<Int, UIButton>
    internal lateinit var terrainPalette: Container
    internal val terrainButtons = mutableMapOf<String, UIButton>()
    internal var selectionBorder: SolidRect? = null

    init {
        lateinit var rowsStepper: Pair<UIText, Map<Int, UIButton>>
        lateinit var columnsStepper: Pair<UIText, Map<Int, UIButton>>
        uiVerticalStack(padding = 10.0) {
            rowsStepper = stepper(label = texts[UiText.MAP_BUILDER_HUD_ROWS]) { step -> delegate?.sizeChanged(rows = rows + step, columns = columns) }
            columnsStepper = stepper(label = texts[UiText.MAP_BUILDER_HUD_COLUMNS]) { step -> delegate?.sizeChanged(rows = rows, columns = columns + step) }
            uiText(text = texts[UiText.MAP_BUILDER_HUD_TERRAIN], size = Size(width = LABEL_WIDTH, height = 20)) { styles.textSize = 15.0 }
            terrainPalette = container()
        }
        rowsValue = rowsStepper.first
        rowsStepButtons = rowsStepper.second
        columnsValue = columnsStepper.first
        columnsStepButtons = columnsStepper.second
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays the map size, disabling the steps that would take it out of the [allowedSizes]. */
    open fun displaySize(
        rows: Int,
        columns: Int,
        allowedSizes: IntRange,
    ) {
        this.rows = rows
        this.columns = columns
        this.allowedSizes = allowedSizes
        rowsValue.text = "$rows"
        columnsValue.text = "$columns"
        rowsStepButtons.forEach { (step, button) -> button.enabled = isAllowedSize(rows + step) }
        columnsStepButtons.forEach { (step, button) -> button.enabled = isAllowedSize(columns + step) }
    }

    /** Displays one button per terrain, with its tile image, and highlights the selected one. */
    open fun displayTerrains(
        terrains: List<TerrainOption>,
        selectedTerrainId: String,
    ) {
        terrainPalette.removeChildren()
        terrainButtons.clear()
        terrainPalette.uiHorizontalStack(padding = 10.0) {
            terrains.forEach { terrain ->
                terrainButtons[terrain.terrainId] =
                    uiButton(label = "", size = Size(TERRAIN_BUTTON_SIZE, TERRAIN_BUTTON_SIZE)) {
                        bgColorOut = Colors.TRANSPARENT
                        bgColorOver = Colors.TRANSPARENT
                        background.borderColor = Colors.TRANSPARENT
                        image(terrain.bitmap) {
                            name = TERRAIN_IMAGE
                            scale = TERRAIN_SCALE
                            smoothing = false
                        }
                        onClick { delegate?.terrainSelected(terrain.terrainId) }
                    }
            }
        }
        selectTerrain(selectedTerrainId)
    }

    /** Highlights the selected terrain with a border around its button. */
    open fun selectTerrain(terrainId: String) {
        selectionBorder?.removeFromParent()
        selectionBorder = null
        val terrainButton = terrainButtons[terrainId] ?: return
        val buttonPosition = terrainButton.parent!!.pos + terrainButton.pos
        val borderSize = TERRAIN_BUTTON_SIZE + 2 * SELECTION_BORDER
        selectionBorder =
            terrainPalette.solidRect(Size(borderSize, borderSize), color = Colors.WHITE) {
                x = buttonPosition.x - SELECTION_BORDER
                y = buttonPosition.y - SELECTION_BORDER
            }
        // Drawn below the terrain buttons, so only the border shows around the selected one.
        terrainPalette.sendChildToBack(selectionBorder!!)
    }

    private fun isAllowedSize(size: Int): Boolean = size in allowedSizes

    // A label, the buttons stepping the size down, the size, and the buttons stepping it up.
    private fun Container.stepper(
        label: String,
        onStep: (Int) -> Unit,
    ): Pair<UIText, Map<Int, UIButton>> {
        lateinit var value: UIText
        val stepButtons = mutableMapOf<Int, UIButton>()
        uiHorizontalStack(padding = 4.0) {
            uiText(text = label, size = Size(width = LABEL_WIDTH, height = 32)) {
                styles.textSize = 15.0
                styles.textAlignment = TextAlignment.MIDDLE_LEFT
            }
            SIZE_STEPS.filter { step -> step < 0 }.forEach { step -> stepButtons[step] = stepButton(step, onStep) }
            value =
                uiText(text = "", size = Size(width = VALUE_WIDTH, height = 32)) {
                    styles.textSize = 15.0
                    styles.textAlignment = TextAlignment.MIDDLE_CENTER
                }
            SIZE_STEPS.filter { step -> step > 0 }.forEach { step -> stepButtons[step] = stepButton(step, onStep) }
        }
        return value to stepButtons
    }

    private fun Container.stepButton(
        step: Int,
        onStep: (Int) -> Unit,
    ): UIButton =
        uiButton(label = if (step > 0) "+$step" else "$step", size = Size(STEP_BUTTON_WIDTH, 32.0)) {
            bgColorOut = Colors.DIMGRAY
            background.borderColor = Colors.LIGHTGRAY
            onClick { onStep(step) }
        }
}
