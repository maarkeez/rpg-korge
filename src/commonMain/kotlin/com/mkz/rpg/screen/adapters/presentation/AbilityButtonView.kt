package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Matrix4
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

class AbilityButtonView(
    size: Size,
    private val resourcesLoader: GameSceneResourcesLoader,
) : UIButton(size) {
    companion object {
        const val ABILITY = "ABILITY"
        const val ABILITY_SELECTION = "ABILITY_SELECTION"
        const val COOLDOWN = "COOLDOWN"

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

        const val SLOT = "SLOT"
        const val COST = "COST"

        private const val COOLDOWN_TEXT_SIZE = 23.0
        private val COOLDOWN_OVERLAY_COLOR = RGBA(0, 0, 0, 0x70)

        private const val SELECTION_THICKNESS = 2.0
        private const val GLOW_THICKNESS = 3.0
        private const val GLOW_ALPHA = 0.3

        private const val COST_TEXT_SIZE = 10.0
        private const val COST_MARGIN = 2.0
    }

    private var delegate: Delegate? = null
    private var abilityId: String? = null

    init {
        bgColorOut = Colors.TRANSPARENT
        bgColorOver = Colors.TRANSPARENT
        background.borderColor = Colors.TRANSPARENT
        background.bgColor = Colors.TRANSPARENT
        panel(size, fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS).name = SLOT

        onClick {
            if (delegate == null) return@onClick
            if (abilityId == null) return@onClick
            delegate?.abilitySelected(abilityId = abilityId!!)
        }
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(
        abilityId: String,
        remainingCooldown: Int,
        hasEnoughMana: Boolean,
        manaCost: Int,
    ) {
        val abilityButton = this
        this.abilityId = abilityId
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        findViewByName(COOLDOWN)?.removeFromParent()
        findViewByName(COST)?.removeFromParent()
        val isInCooldown = remainingCooldown > 0
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
        if (isInCooldown) {
            displayCooldownOverlay(remainingCooldown)
        } else if (!hasEnoughMana) {
            displayCostBadge(manaCost)
        }
        visible = true
    }

    private fun displayCooldownOverlay(remainingCooldown: Int) {
        val abilityButton = this
        abilityButton.container {
            name = COOLDOWN
            roundRect(
                Size(abilityButton.width - 2 * SELECTION_THICKNESS, abilityButton.height - 2 * SELECTION_THICKNESS),
                RectCorners(HudStyle.SLOT_RADIUS - SELECTION_THICKNESS),
                fill = COOLDOWN_OVERLAY_COLOR,
            ) { xy(SELECTION_THICKNESS, SELECTION_THICKNESS) }
            text(text = "$remainingCooldown", textSize = COOLDOWN_TEXT_SIZE, color = Colors.WHITE) {
                centerOn(abilityButton)
            }
        }
    }

    private fun displayCostBadge(manaCost: Int) {
        val abilityButton = this
        val badge = PillView(color = HudStyle.MANA, glyph = PillView.Glyph.MANA, solid = true, textSize = COST_TEXT_SIZE)
        badge.name = COST
        badge.display("$manaCost")
        abilityButton.addChild(badge)
        badge.xy(abilityButton.width - badge.width - COST_MARGIN, abilityButton.height - badge.pillHeight - COST_MARGIN)
    }

    fun select() {
        if (findViewByName(ABILITY_SELECTION) != null) return
        val abilityButton = this
        val selection =
            Container().apply {
                name = ABILITY_SELECTION
                roundRect(
                    Size(abilityButton.width + 2 * GLOW_THICKNESS, abilityButton.height + 2 * GLOW_THICKNESS),
                    RectCorners(HudStyle.SLOT_RADIUS + GLOW_THICKNESS),
                    fill = HudStyle.SELECTION.withAd(GLOW_ALPHA),
                ) { xy(-GLOW_THICKNESS, -GLOW_THICKNESS) }
                roundRect(abilityButton.size, RectCorners(HudStyle.SLOT_RADIUS), fill = HudStyle.SELECTION)
                roundRect(
                    Size(abilityButton.width - 2 * SELECTION_THICKNESS, abilityButton.height - 2 * SELECTION_THICKNESS),
                    RectCorners(HudStyle.SLOT_RADIUS - SELECTION_THICKNESS),
                    fill = HudStyle.PANEL_RAISED,
                ) { xy(SELECTION_THICKNESS, SELECTION_THICKNESS) }
            }
        val slot = findViewByName(SLOT)!!
        addChildAt(selection, slot.index + 1)
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
