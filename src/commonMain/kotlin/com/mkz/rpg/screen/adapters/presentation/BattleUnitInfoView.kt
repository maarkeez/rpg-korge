package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animator
import korlibs.korge.animate.moveTo
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds

open class BattleUnitInfoView(
    resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : Container() {
    companion object {
        val MOVEMENT_POINTS_COLOR = Colors.WHITE
        val CAST_POINTS_COLOR = RGBA(0xFF, 0xBB, 0x31)

        const val WIDTH = 390.0
        private const val MARGIN = 4.0
        private const val PANEL_WIDTH = WIDTH - 2 * MARGIN
        private const val HEADER_HEIGHT = 120.0
        private const val PORTRAIT_SIZE = 104.0
        private const val CONTENT_X = MARGIN + 122
        private const val CONTENT_RIGHT = WIDTH - MARGIN - 8
        private const val NAME_TEXT_SIZE = 18.0
        private const val CAPTION_TEXT_SIZE = 10.0
        private const val CAPTION_WIDTH = 24.0
        private const val CAPTION_SPACING = 6.0
        private const val TURN_ACTIONS_SPACING = 14.0
        private const val INVENTORY_BUTTON_SIZE = 40.0

        private const val STRIP_HEIGHT = 52.0
        private const val STRIP_PORTRAIT_SIZE = 40.0
        private const val STRIP_CONTENT_X = MARGIN + 58
        private const val STRIP_BARS_RIGHT = WIDTH - MARGIN - 112
        private const val STRIP_ACTIONS_X = STRIP_BARS_RIGHT + 14
        private const val STRIP_POINTS_X = STRIP_ACTIONS_X + 44

        const val ABILITY_BUTTON_SIZE = 54.0
        private const val ABILITIES = 6
        private const val ABILITY_BUTTONS_GAP = (PANEL_WIDTH - ABILITIES * ABILITY_BUTTON_SIZE) / (ABILITIES - 1)
        private const val SPACING = 8.0
        internal const val ABILITIES_Y = HEADER_HEIGHT + SPACING
        internal const val FOCUSED_ABILITIES_Y = STRIP_HEIGHT + SPACING

        val FOCUS_DURATION = 150.milliseconds
    }

    data class TurnActions(
        val remainingSteps: Int,
        val maximumSteps: Int,
        val remainingCasts: Int,
        val maximumCasts: Int,
    )

    data class Ability(
        val abilityId: String,
        val remainingCooldown: Int,
        val hasEnoughMana: Boolean,
        val manaCost: Int,
    )

    internal val header: Container = container { panel(Size(PANEL_WIDTH, HEADER_HEIGHT)).xy(MARGIN, 0.0) }
    internal val unitPortraitView = UnitPortraitView(Size(PORTRAIT_SIZE, PORTRAIT_SIZE), resourcesLoader).addTo(header, MARGIN + 8, 8.0)
    internal val nameText: Text = header.text("", textSize = NAME_TEXT_SIZE, color = Colors.WHITE) { xy(CONTENT_X, 9.0) }
    internal val remainingSummonDurationView = RemainingSummonDurationView(Size(160, 18), messages).addTo(header, CONTENT_X, 8.0)
    internal val inventoryButton = InventoryButtonView(Size(INVENTORY_BUTTON_SIZE, INVENTORY_BUTTON_SIZE), resourcesLoader).addTo(header, CONTENT_RIGHT - INVENTORY_BUTTON_SIZE, 8.0)
    private val moveCaption: Text = header.caption(UiMessage.HUD_MOVE, CONTENT_X, 39.0)
    internal val movementPointsView = PointsView(MOVEMENT_POINTS_COLOR).addTo(header, 0.0, 41.0)
    private val castCaption: Text = header.caption(UiMessage.HUD_CAST, 0.0, 39.0)
    internal val castPointsView = PointsView(CAST_POINTS_COLOR).addTo(header, 0.0, 41.0)
    private val healthBarView = HealthBarView(Size(CONTENT_RIGHT - CONTENT_X - CAPTION_WIDTH, 14)).addTo(header, CONTENT_X + CAPTION_WIDTH, 56.0)
    private val manaBarView = ManaBarView(Size(CONTENT_RIGHT - CONTENT_X - CAPTION_WIDTH, 14)).addTo(header, CONTENT_X + CAPTION_WIDTH, 74.0)
    internal val effectsView = EffectsView(resourcesLoader).addTo(header, CONTENT_X, 94.0)

    internal val strip: Container =
        container {
            panel(Size(PANEL_WIDTH, STRIP_HEIGHT)).xy(MARGIN, 0.0)
            visible = false
        }
    internal val stripPortraitView = UnitPortraitView(Size(STRIP_PORTRAIT_SIZE, STRIP_PORTRAIT_SIZE), resourcesLoader, portraitScale = 1.0).addTo(strip, MARGIN + 6, 6.0)
    private val stripHealthBarView = HealthBarView(Size(STRIP_BARS_RIGHT - STRIP_CONTENT_X - CAPTION_WIDTH, 14)).addTo(strip, STRIP_CONTENT_X + CAPTION_WIDTH, 9.0)
    private val stripManaBarView = ManaBarView(Size(STRIP_BARS_RIGHT - STRIP_CONTENT_X - CAPTION_WIDTH, 14)).addTo(strip, STRIP_CONTENT_X + CAPTION_WIDTH, 27.0)
    internal val stripMovementPointsView = PointsView(MOVEMENT_POINTS_COLOR).addTo(strip, STRIP_POINTS_X, 13.0)
    internal val stripCastPointsView = PointsView(CAST_POINTS_COLOR).addTo(strip, STRIP_POINTS_X, 31.0)

    internal val body: Container = container { y = ABILITIES_Y }
    internal val abilityButtons: Array<AbilityButtonView> =
        Array(ABILITIES) { index ->
            AbilityButtonView(Size(ABILITY_BUTTON_SIZE, ABILITY_BUTTON_SIZE), resourcesLoader).addTo(body, MARGIN + index * (ABILITY_BUTTON_SIZE + ABILITY_BUTTONS_GAP), 0.0)
        }
    internal val abilityInfoView = AbilityInfoView(AbilityInfoView.WIDTH, resourcesLoader, messages).addTo(body, (WIDTH - AbilityInfoView.WIDTH) / 2, ABILITY_BUTTON_SIZE + SPACING)

    internal val focusAnimator: Animator = body.animator(parallel = true)

    internal var isFocused = false
        private set

    init {
        header.caption(UiMessage.HUD_HEALTH, CONTENT_X, 58.0)
        header.caption(UiMessage.HUD_MANA, CONTENT_X, 76.0)
        strip.caption(UiMessage.HUD_HEALTH, STRIP_CONTENT_X, 11.0)
        strip.caption(UiMessage.HUD_MANA, STRIP_CONTENT_X, 29.0)
        strip.caption(UiMessage.HUD_MOVE, STRIP_ACTIONS_X, 11.0)
        strip.caption(UiMessage.HUD_CAST, STRIP_ACTIONS_X, 29.0)
        visible = false
    }

    open fun setDelegate(delegate: AbilityButtonView.Delegate) {
        abilityButtons.forEach { abilityButton -> abilityButton.setDelegate(delegate) }
    }

    open fun display(
        battleUnit: BattleUnitInfo,
        turnActions: TurnActions,
        remainingTurns: Int?,
        abilities: List<Ability>,
    ) {
        abilityButtons.forEach(AbilityButtonView::hide)
        abilityInfoView.hide()
        expand()
        val ring = if (battleUnit.isHumanPlayers) HudStyle.ALLY else HudStyle.ENEMY
        unitPortraitView.display(battleUnit.unitId, ring = ring)
        stripPortraitView.display(battleUnit.unitId, ring = ring)
        nameText.text = messages.unitName(battleUnit.unitId)
        if (remainingTurns != null) remainingSummonDurationView.display(remainingTurns) else remainingSummonDurationView.hide()
        remainingSummonDurationView.x = nameText.x + nameText.width + 8
        displayTurnActions(turnActions)
        healthBarView.display(remaining = battleUnit.remainingHealth, maximum = battleUnit.maximumHealth)
        manaBarView.display(remaining = battleUnit.remainingMana, maximum = battleUnit.maximumMana)
        stripHealthBarView.display(remaining = battleUnit.remainingHealth, maximum = battleUnit.maximumHealth)
        stripManaBarView.display(remaining = battleUnit.remainingMana, maximum = battleUnit.maximumMana)
        abilities.forEachIndexed { index, ability ->
            abilityButtons[index].display(
                abilityId = ability.abilityId,
                remainingCooldown = ability.remainingCooldown,
                hasEnoughMana = ability.hasEnoughMana,
                manaCost = ability.manaCost,
            )
        }
        effectsView.display(effectIds = battleUnit.effectIds)
        visible = true
    }

    open fun displayInventoryButton(onInventoryButtonSelected: () -> kotlin.Unit) {
        inventoryButton.display(onInventoryButtonSelected)
    }

    open fun hideInventoryButton() {
        inventoryButton.hide()
    }

    open fun displayAbilitySelected(index: Int) {
        abilityButtons.forEach(AbilityButtonView::unselect)
        abilityButtons[index].select()
    }

    open fun displayAbilityInfo(abilityInfo: AbilityInfoView.AbilityInfo) {
        abilityInfoView.display(abilityInfo)
        focus()
    }

    open fun hideAbilityInfo() {
        abilityInfoView.hide()
        expand()
    }

    open fun hide() {
        visible = false
    }

    private fun displayTurnActions(turnActions: TurnActions) {
        movementPointsView.display(remaining = turnActions.remainingSteps, maximum = turnActions.maximumSteps)
        castPointsView.display(remaining = turnActions.remainingCasts, maximum = turnActions.maximumCasts)
        movementPointsView.x = moveCaption.x + moveCaption.width + CAPTION_SPACING
        castCaption.x = movementPointsView.x + movementPointsView.width + TURN_ACTIONS_SPACING
        castPointsView.x = castCaption.x + castCaption.width + CAPTION_SPACING
        stripMovementPointsView.display(remaining = turnActions.remainingSteps, maximum = turnActions.maximumSteps)
        stripCastPointsView.display(remaining = turnActions.remainingCasts, maximum = turnActions.maximumCasts)
    }

    private fun focus() {
        header.visible = false
        strip.visible = true
        isFocused = true
        slideBody(to = FOCUSED_ABILITIES_Y)
    }

    private fun expand() {
        header.visible = true
        strip.visible = false
        isFocused = false
        slideBody(to = ABILITIES_Y)
    }

    private fun slideBody(to: Double) {
        focusAnimator.cancel()
        if (body.y == to) return
        focusAnimator.moveTo(view = body, x = { 0.0 }, y = { to }, time = FOCUS_DURATION, easing = Easing.EASE_OUT_QUAD)
    }

    private fun Container.caption(
        text: UiMessage,
        x: Double,
        y: Double,
    ): Text = text(messages[text], textSize = CAPTION_TEXT_SIZE, color = HudStyle.FAINT) { xy(x, y) }

    private fun <T : korlibs.korge.view.View> T.addTo(
        parent: Container,
        x: Double,
        y: Double,
    ): T = also { view -> parent.addChild(view) }.apply { xy(x, y) }
}
