package screen

import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*

class UnitPortraitView(size: Size): UIContainer(size) {

    private val battleUnitPortrait = uiButton("").also { button ->
        button.size = size
        button.bgColorOut = Colors.WHITE
        button.bgColorOver = Colors.LIGHTSKYBLUE
        button.background.borderColor = Colors.LIGHTGRAY
    }
    private lateinit var knightPortrait: Bitmap
    private lateinit var ratPortrait: Bitmap

    suspend fun loadAssets() {
        knightPortrait = resourcesVfs["unit/knight_portrait.png"].readBitmap()
        ratPortrait = resourcesVfs["unit/rat_portrait.png"].readBitmap()
    }

    init {
        addChild(battleUnitPortrait)
    }

    fun display(unitId: String) {
        battleUnitPortrait.findViewByName("portrait")?.removeFromParent()
        when(unitId) {
            "knight" -> knightPortrait
            "rat" -> ratPortrait
            else -> null
        }?.let { avatarBitmap ->
            battleUnitPortrait.image(avatarBitmap) {
                name = "portrait"
                smoothing = false
                scale = 3.0
                centerOn(battleUnitPortrait)
            }
        }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
