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

    private val battleUnitAvatar = uiButton("").also { button ->
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
        addChild(battleUnitAvatar)
    }

    fun display(unitId: String) {
        // Avatar
        battleUnitAvatar.findViewByName("avatar")?.removeFromParent()
        when(unitId) {
            "knight" -> knightPortrait
            "rat" -> ratPortrait
            else -> null
        }?.let { avatarBitmap ->
            battleUnitAvatar.image(avatarBitmap) {
                name = "avatar"
                smoothing = false
                scale = 3.0
                centerOn(battleUnitAvatar)
            }
        }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
