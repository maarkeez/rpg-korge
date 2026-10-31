package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
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

        const val SLOT = "SLOT"
        const val COST = "COST"

        // In cooldown: a dark overlay over the darkened icon, with the turns left large in its middle.
        private const val COOLDOWN_TEXT_SIZE = 23.0
        private val COOLDOWN_OVERLAY_COLOR = RGBA(0, 0, 0, 0x70)

        // Selected: a gold ring, with a faint glow around it.
        private const val SELECTION_THICKNESS = 2.0
        private const val GLOW_THICKNESS = 3.0
        private const val GLOW_ALPHA = 0.3

        // The mana cost badge, at the bottom right corner, over the icon.
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

    /**
     * Displays the ability at 3x in its slot, darkened under a dark overlay with the turns left in its middle while in
     * cooldown, else tinted blue with its [manaCost] in a badge when there is not enough mana to cast it. The cooldown
     * wins, as it is the longer wait.
     */
    fun display(
        abilityId: String,
        cooldownTurnsLeft: Int,
        hasEnoughMana: Boolean,
        manaCost: Int,
    ) {
        val abilityButton = this
        this.abilityId = abilityId
        findViewByName(ABILITY)?.removeFromParent()
        findViewByName(ABILITY_SELECTION)?.removeFromParent()
        findViewByName(COOLDOWN)?.removeFromParent()
        findViewByName(COST)?.removeFromParent()
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
        if (isInCooldown) {
            displayCooldownOverlay(cooldownTurnsLeft)
        } else if (!hasEnoughMana) {
            displayCostBadge(manaCost)
        }
        visible = true
    }

    private fun displayCooldownOverlay(cooldownTurnsLeft: Int) {
        val abilityButton = this
        abilityButton.container {
            name = COOLDOWN
            roundRect(abilityButton.size, RectCorners(HudStyle.SLOT_RADIUS), fill = COOLDOWN_OVERLAY_COLOR)
            text(text = "$cooldownTurnsLeft", textSize = COOLDOWN_TEXT_SIZE, color = Colors.WHITE) {
                centerOn(this@container)
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

    /** Rings the ability in gold, with a faint glow around it. */
    fun select() {
        if (findViewByName(ABILITY_SELECTION) != null) return
        val abilityButton = this
        abilityButton.container {
            name = ABILITY_SELECTION
            // Each ring is inset by half its thickness, as a stroke is drawn half outside its shape, which cuts it.
            roundRect(
                Size(abilityButton.width + GLOW_THICKNESS, abilityButton.height + GLOW_THICKNESS),
                RectCorners(HudStyle.SLOT_RADIUS + GLOW_THICKNESS / 2),
                fill = Colors.TRANSPARENT,
                stroke = HudStyle.SELECTION.withAd(GLOW_ALPHA),
                strokeThickness = GLOW_THICKNESS,
            ) { xy(-GLOW_THICKNESS / 2, -GLOW_THICKNESS / 2) }
            roundRect(
                Size(abilityButton.width - SELECTION_THICKNESS, abilityButton.height - SELECTION_THICKNESS),
                RectCorners(HudStyle.SLOT_RADIUS - SELECTION_THICKNESS / 2),
                fill = Colors.TRANSPARENT,
                stroke = HudStyle.SELECTION,
                strokeThickness = SELECTION_THICKNESS,
            ) { xy(SELECTION_THICKNESS / 2, SELECTION_THICKNESS / 2) }
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
