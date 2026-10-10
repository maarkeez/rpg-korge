package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability.Status
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter
import korlibs.korge.view.image
import korlibs.math.geom.Matrix4
import korlibs.math.geom.Size

class AbilityButtonView(
    size: Size,
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : UIButton(size) {
    companion object {
        const val ABILITY = "ABILITY"
        const val ABILITY_SELECTION = "ABILITY_SELECTION"
        const val ABILITY_STATUS = "ABILITY_STATUS"
        const val ABILITY_COOLDOWN_NUMBER = "ABILITY_COOLDOWN_NUMBER"
        const val ABILITY_MANA_GLYPH = "ABILITY_MANA_GLYPH"
        const val ABILITY_COST_NUMBER = "ABILITY_COST_NUMBER"
        const val ABILITY_LOCK_GLYPH = "ABILITY_LOCK_GLYPH"
        private const val FRAME_THICKNESS = 3
        private const val EDGE_MARGIN = 3
    }

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
        sprites.load()
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Availability of the displayed ability. `null` until [display] is called. */
    var status: Status? = null
        private set

    /**
     * Ready abilities show the plain icon. Every other status dims the icon and adds a shape that doesn't
     * rely on color: a big turns-left number, a mana drop with the cost, or a lock.
     */
    fun display(availability: AbilityAvailability) {
        val abilityButton = this
        abilityId = availability.abilityId
        status = availability.status
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_STATUS)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        abilityButton.image(sprites.ability(availability.abilityId)) {
            name = ABILITY
            smoothing = false
            scale = PIXEL_SCALE.toDouble()
            centerOn(abilityButton)
            if (availability.status != Status.READY) {
                filter = darkFilter()
            }
        }
        if (availability.status != Status.READY) {
            addChild(statusGlyphs(availability))
        }
        visible = true
    }

    fun select() {
        if (findViewByName(ABILITY_SELECTION) != null) return
        val frame = Container().also { it.name = ABILITY_SELECTION }
        val width = this.width
        val height = this.height
        val thickness = FRAME_THICKNESS.toDouble()
        listOf(
            Size(width, thickness) to (0.0 to 0.0),
            Size(width, thickness) to (0.0 to height - thickness),
            Size(thickness, height) to (0.0 to 0.0),
            Size(thickness, height) to (width - thickness to 0.0),
        ).forEach { (size, position) ->
            frame.addChild(
                SolidRect(size, UiPalette.selection).also {
                    it.x = position.first
                    it.y = position.second
                },
            )
        }
        addChild(frame)
    }

    fun unselect() {
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
    }

    private fun statusGlyphs(availability: AbilityAvailability): Container {
        val glyphs = Container().also { it.name = ABILITY_STATUS }
        when (availability.status) {
            Status.COOLDOWN -> {
                val scale = PIXEL_SCALE * 2
                val number = PixelGlyphs.number(availability.cooldownTurnsLeft, UiPalette.textPrimary, scale, ABILITY_COOLDOWN_NUMBER)
                number.x = ((width - number.pixelWidth) / 2).toInt().toDouble()
                number.y = ((height - number.pixelHeight) / 2).toInt().toDouble()
                glyphs.addChild(number)
            }
            Status.NOT_ENOUGH_MANA -> {
                val drop = PixelGlyphs.glyph(PixelGlyphs.MANA_DROP, UiPalette.manaCost, PIXEL_SCALE, ABILITY_MANA_GLYPH)
                drop.x = EDGE_MARGIN.toDouble()
                drop.y = EDGE_MARGIN.toDouble()
                glyphs.addChild(drop)
                val cost = PixelGlyphs.number(availability.cost, UiPalette.danger, PIXEL_SCALE, ABILITY_COST_NUMBER)
                cost.x = (width - cost.pixelWidth - EDGE_MARGIN).toInt().toDouble()
                cost.y = (height - cost.pixelHeight - EDGE_MARGIN).toInt().toDouble()
                glyphs.addChild(cost)
            }
            Status.NO_CASTS_LEFT -> {
                val lock = PixelGlyphs.glyph(PixelGlyphs.LOCK, UiPalette.textPrimary, PIXEL_SCALE, ABILITY_LOCK_GLYPH)
                lock.x = ((width - 7 * PIXEL_SCALE) / 2).toInt().toDouble()
                lock.y = ((height - 7 * PIXEL_SCALE) / 2).toInt().toDouble()
                glyphs.addChild(lock)
            }
            Status.READY -> Unit
        }
        return glyphs
    }

    private fun darkFilter(): ColorMatrixFilter =
        ColorMatrixFilter(
            Matrix4.fromRows(
                0.5f,
                0f,
                0f,
                0f,
                0f,
                0.5f,
                0f,
                0f,
                0f,
                0f,
                0.5f,
                0f,
                0f,
                0f,
                0f,
                1f,
            ),
        )

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun abilitySelected(abilityId: String)
    }
}
