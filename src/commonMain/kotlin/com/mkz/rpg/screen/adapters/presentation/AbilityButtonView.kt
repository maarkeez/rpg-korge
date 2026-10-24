package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Matrix4
import korlibs.math.geom.Size

class AbilityButtonView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIButton(size) {
    companion object {
        const val ABILITY = "ABILITY"
        const val ABILITY_SELECTION = "ABILITY_SELECTION"
        const val COOLDOWN = "COOLDOWN"

        // In cooldown: halves the colours.
        internal val DARK_MATRIX =
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
            )

        // Not enough mana: the icon in shades of blue, as the mana bar, from its luminance (0.299 red, 0.587 green, 0.114 blue).
        internal val BLUE_MATRIX =
            Matrix4.fromRows(
                0.06f,
                0.12f,
                0.02f,
                0f,
                0.18f,
                0.35f,
                0.07f,
                0f,
                0.39f,
                0.76f,
                0.15f,
                0f,
                0f,
                0f,
                0f,
                1f,
            )

        private const val COOLDOWN_TEXT_SIZE = 14.0
        private const val COOLDOWN_BADGE_SIZE = 16.0
        private val COOLDOWN_BADGE_COLOR = RGBA(0, 0, 0, 0xB0)
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

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /**
     * Displays the ability, darkened with the turns left in a corner badge while in cooldown, else tinted blue when
     * there is not enough mana to cast it. The cooldown wins, as it is the longer wait.
     */
    fun display(
        abilityId: String,
        cooldownTurnsLeft: Int,
        hasEnoughMana: Boolean,
    ) {
        val abilityButton = this
        this.abilityId = abilityId
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        findViewByName(COOLDOWN)?.removeFromParent()
        val isInCooldown = cooldownTurnsLeft > 0
        resourcesLoader.bitmapOrNull("ability/$abilityId.png")?.let { avatarBitmap ->
            abilityButton.image(avatarBitmap) {
                name = ABILITY
                smoothing = false
                scale = 3.0
                centerOn(abilityButton)
                filter =
                    when {
                        isInCooldown -> ColorMatrixFilter(DARK_MATRIX)
                        !hasEnoughMana -> ColorMatrixFilter(BLUE_MATRIX)
                        else -> null
                    }
            }
        }
        if (isInCooldown) displayCooldownBadge(cooldownTurnsLeft)
        visible = true
    }

    private fun displayCooldownBadge(cooldownTurnsLeft: Int) {
        val abilityButton = this
        abilityButton.container {
            name = COOLDOWN
            solidRect(Size(COOLDOWN_BADGE_SIZE, COOLDOWN_BADGE_SIZE), COOLDOWN_BADGE_COLOR)
            text(text = "$cooldownTurnsLeft", textSize = COOLDOWN_TEXT_SIZE, color = Colors.WHITE) {
                centerOn(this@container)
            }
            x = abilityButton.width - COOLDOWN_BADGE_SIZE
            y = abilityButton.height - COOLDOWN_BADGE_SIZE
        }
    }

    fun select() {
        if (findViewByName(ABILITY_SELECTION) != null) return
        val abilityButton = this
        abilityButton.image(resourcesLoader.bitmap("ability/ability_selection.png")) {
            name = ABILITY_SELECTION
            smoothing = false
            scale = 3.0
            centerOn(abilityButton)
        }
    }

    fun unselect() {
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
    }

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun abilitySelected(abilityId: String)
    }
}
