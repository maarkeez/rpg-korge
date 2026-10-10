package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
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

    fun display(
        abilityId: String,
        canCast: Boolean,
    ) {
        val abilityButton = this
        this.abilityId = abilityId
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        abilityButton.image(sprites.ability(abilityId)) {
            name = ABILITY
            smoothing = false
            scale = PIXEL_SCALE.toDouble()
            centerOn(abilityButton)
            if (!canCast) {
                filter = darkFilter()
            }
        }
        visible = true
    }

    fun select() {
        if (findViewByName(ABILITY_SELECTION) != null) return
        val abilityButton = this
        abilityButton.image(sprites.abilitySelection()) {
            name = ABILITY_SELECTION
            smoothing = false
            scale = PIXEL_SCALE.toDouble()
            centerOn(abilityButton)
        }
    }

    fun unselect() {
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
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
