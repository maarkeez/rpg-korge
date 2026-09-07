package screen

import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.korge.view.filter.*
import korlibs.math.geom.*

class AbilityButtonView(size: Size) : UIButton(size) {

    companion object {
        const val ABILITY = "ABILITY"
        const val ABILITY_SELECTION = "ABILITY_SELECTION"
    }

    private lateinit var heal: Bitmap
    private lateinit var sword: Bitmap
    private lateinit var poisonedSword: Bitmap
    private lateinit var mushroom: Bitmap
    private lateinit var skull: Bitmap
    private lateinit var teleport: Bitmap
    private lateinit var bee: Bitmap
    private lateinit var abilitySelection: Bitmap

    private var delegate: Delegate? = null
    private var abilityId: String? = null

    init {
        bgColorOut = Colors.TRANSPARENT
        bgColorOver = Colors.TRANSPARENT
        background.borderColor = Colors.TRANSPARENT
        background.bgColor = Colors.TRANSPARENT

        onClick {
            if (delegate == null) return@onClick
            if (abilityId == null) return@onClick
            delegate?.abilitySelected(abilityId = abilityId!!)
        }
    }

    suspend fun loadAssets() {
        heal = resourcesVfs["ability/heal.png"].readBitmap()
        sword = resourcesVfs["ability/sword.png"].readBitmap()
        poisonedSword = resourcesVfs["ability/poisoned_sword.png"].readBitmap()
        mushroom = resourcesVfs["ability/mushroom.png"].readBitmap()
        skull = resourcesVfs["ability/skull.png"].readBitmap()
        teleport = resourcesVfs["ability/teleport.png"].readBitmap()
        bee = resourcesVfs["ability/bee.png"].readBitmap()
        abilitySelection = resourcesVfs["ability/ability_selection.png"].readBitmap()
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(abilityId: String, canCast: Boolean) {
        val abilityButton = this
        this.abilityId = abilityId
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        when (abilityId) {
            "heal" -> heal
            "poisoned-sword" -> poisonedSword
            "sword" -> sword
            "mushroom" -> mushroom
            "skull" -> skull
            "teleport" -> teleport
            "bee" -> bee
            else -> null
        }?.let { avatarBitmap ->
            abilityButton.image(avatarBitmap) {
                name = ABILITY
                smoothing = false
                scale = 3.0
                centerOn(abilityButton)
                if (!canCast) {
                    filter = darkFilter()
                }
            }
        }
        visible = true
    }

    fun select() {
        if(findViewByName(ABILITY_SELECTION) != null) return
        val abilityButton = this
        abilityButton.image(abilitySelection) {
            name = ABILITY_SELECTION
            smoothing = false
            scale = 3.0
            centerOn(abilityButton)
        }
    }

    fun unselect() {
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
    }

    private fun darkFilter(): ColorMatrixFilter = ColorMatrixFilter(
        Matrix4.fromRows(
            0.5f, 0f, 0f, 0f,
            0f, 0.5f, 0f, 0f,
            0f, 0f, 0.5f, 0f,
            0f, 0f, 0f, 1f
        )
    )

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun abilitySelected(abilityId: String)
    }
}
