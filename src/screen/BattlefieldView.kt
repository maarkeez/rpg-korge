package screen

import battlefield.domain.Battlefield
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.view.Container
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.*

class BattlefieldView: Container() {

    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"
    }
    private var delegate: Delegate? = null
    private lateinit var sandBitmap: Bitmap
    private lateinit var knightBitmap: Bitmap
    private lateinit var ratBitmap: Bitmap
    private lateinit var tileSelection1BitMap: Bitmap
    private lateinit var tileSelection2BitMap: Bitmap
    private lateinit var tileSelection3BitMap: Bitmap
    private lateinit var unitSelectionBitMap: Bitmap

    private val battlefieldGrid = uiGridFill(
        size= Size(width=384, height=384),
        spacing = Spacing(0.0,0.0),
        cols = 0,
        rows = 0)


    init {
        addChild(battlefieldGrid)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    suspend fun loadAssets() {
        sandBitmap = resourcesVfs["terrain/sand.png"].readBitmap()
        knightBitmap = resourcesVfs["unit/knight.png"].readBitmap()
        ratBitmap = resourcesVfs["unit/rat.png"].readBitmap()
        tileSelection1BitMap = resourcesVfs["battlefield/tile_selection_1.png"].readBitmap()
        tileSelection2BitMap = resourcesVfs["battlefield/tile_selection_2.png"].readBitmap()
        tileSelection3BitMap = resourcesVfs["battlefield/tile_selection_3.png"].readBitmap()
        unitSelectionBitMap = resourcesVfs["battlefield/unit_selection.png"].readBitmap()
    }

    fun displayBattlefield(dto: Battlefield.Dto) {
        battlefieldGrid.rows = dto.rows
        battlefieldGrid.cols = dto.columns
        for (row in 0 until dto.rows) {
            for (column in 0 until dto.columns) {
                battlefieldGrid.uiButton(label = "").also { tileButton ->
                    tileButton.bgColorOut = Colors.TRANSPARENT
                    tileButton.bgColorOver = Colors.TRANSPARENT
                    tileButton.background.borderColor = Colors.TRANSPARENT
                    tileButton.background.bgColor = Colors.TRANSPARENT
                    tileButton.name = tileName(row, column)
                    tileButton.addImage(sandBitmap, TERRAIN)
                    tileButton.onClick {
                        delegate?.tileSelected(row, column)
                    }
                }
            }
        }
    }

    private fun tileName(row: Int, column: Int): String = "row-$row-column-$column"

    fun displayKnightBattleUnit(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(knightBitmap, BATTLE_UNIT)
    }

    fun displayRatBattleUnit(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(ratBitmap, BATTLE_UNIT)
    }

    interface Delegate {
        fun tileSelected(row: Int, column: Int)
    }

    fun displayPotentialMovement(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(tileSelection3BitMap, SELECTION)
    }

    fun displayPotentialCast(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        if(tileButton.findViewByName(SELECTION) != null) return
        tileButton.addImage(tileSelection2BitMap, SELECTION)
    }

    fun displayUnitSelection(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addImage(unitSelectionBitMap, SELECTION)
    }

    fun resetTiles() {
        battlefieldGrid.children.forEach { view ->
            val tileButton = view as UIButton
            tileButton.findViewByName(SELECTION)?.removeFromParent()
        }
    }

    fun removeBattleUnit(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
    }

    private fun UIButton.addImage(bitmap: Bitmap, viewName: String) {
        val button = this
        button.image(bitmap ){
            name = viewName
            scale = 3.0
            smoothing = false
            centerOn(button)
        }
    }

}
