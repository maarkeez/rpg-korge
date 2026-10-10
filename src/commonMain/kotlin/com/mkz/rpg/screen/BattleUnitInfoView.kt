package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import com.mkz.rpg.unit.domain.Unit
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.addUpdater
import korlibs.korge.view.image
import korlibs.math.geom.Size
import kotlin.time.DurationUnit

class BattleUnitInfoView(
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : Container() {
    private lateinit var unitNameView: UnitNameView
    private lateinit var turnPipsView: TurnPipsView
    private lateinit var abilityButtons: Array<AbilityButtonView>
    private val statusListView = StatusListView(Size(width = 390.0, height = StatusListView.ROW_HEIGHT.toDouble()), sprites)
    private val readOnlyAbilities = Container()
    private val abilityLine =
        uiText("", size = Size(width = 390.0, height = ABILITY_LINE_HEIGHT)) {
            name = ABILITY_LINE
            styles.textColor = UiPalette.textPrimary
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
            styles.textSize = 12.0
        }
    private var persistentAbilityLine = ""
    private var transientMessageSecondsLeft = 0.0
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    private lateinit var unitPortraitView: UnitPortraitView

    suspend fun loadAssets() {
        unitPortraitView.loadAssets()
        abilityButtons.forEach { abilityButton -> abilityButton.loadAssets() }
        sprites.load()
    }

    private val battleUnitInfoLayout =
        uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), sprites)
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(unitNameView)

                    turnPipsView = TurnPipsView(Size(width = 281.5, height = 14))
                    addChild(turnPipsView)
                    uiSpacing(Size(0, 15))
                    healthBarView = HealthBarView(Size(281.5, 16))
                    addChild(healthBarView)

                    uiSpacing(Size(0, 4))
                    manaBarView = ManaBarView(Size(281.5, 16))
                    addChild(manaBarView)
                }
            }
            uiHorizontalStack(padding = 2.0) {
                val abilityButtonSize = Size(width = 48.75, height = 48.75)
                abilityButtons =
                    arrayOf(
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                    )
                abilityButtons.forEach(::addChild)
            }
        }

    init {
        visible = false
        addChild(battleUnitInfoLayout)
        readOnlyAbilities.y = ABILITY_ROW_Y
        addChild(readOnlyAbilities)
        abilityLine.y = ABILITY_LINE_Y
        addChild(abilityLine)
        statusListView.y = STATUS_LIST_Y
        addChild(statusListView)
        addUpdater { dt ->
            if (transientMessageSecondsLeft > 0.0) {
                transientMessageSecondsLeft -= dt.toDouble(DurationUnit.SECONDS)
                if (transientMessageSecondsLeft <= 0.0) abilityLine.text = persistentAbilityLine
            }
        }
    }

    private companion object {
        const val MAX_CASTS = 1
        const val ART_SIZE = 16
        const val ABILITY_ROW_Y = 102.5
        const val ABILITY_BUTTON_SIZE = 48.75
        const val ABILITY_LINE_HEIGHT = 18.0
        const val ABILITY_LINE_Y = ABILITY_ROW_Y + ABILITY_BUTTON_SIZE + 2.0
        const val STATUS_LIST_Y = ABILITY_LINE_Y + ABILITY_LINE_HEIGHT + 2.0
        const val MESSAGE_SECONDS = 1.5
        const val ABILITY_LINE = "ABILITY_LINE"
    }

    /** The text under the ability bar, for tests. */
    val abilityLineText: String get() = abilityLine.text

    /** The ability bar slots, for tests. */
    val abilitySlots: List<AbilityButtonView> get() = abilityButtons.toList()

    /** Statuses shown in the list, for tests. */
    val displayedStatusRows: Int get() = statusListView.rowCount

    /** Ability buttons that can start a cast. Always zero for units the player can't command. */
    val visibleAbilityButtonCount: Int get() = abilityButtons.count { it.visible }

    val readOnlyAbilityIconCount: Int get() = readOnlyAbilities.numChildren

    fun setDelegate(delegate: AbilityButtonView.Delegate) {
        abilityButtons.forEach { abilityButton -> abilityButton.setDelegate(delegate) }
    }

    fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
        interactive: Boolean = true,
        abilities: List<AbilityAvailability> = emptyList(),
    ) {
        abilityButtons.forEach(AbilityButtonView::hide)
        displayAbilityLine("")
        readOnlyAbilities.removeChildren()
        // Avatar
        unitPortraitView.display(battleUnit.unitId)
        // Name
        unitNameView.display(unitName = unit.name)
        // Remaining turn actions
        turnPipsView.display(
            remainingSteps = battleUnit.remainingTurnActions.remainingSteps,
            maximumSteps = unit.movementRange,
            remainingCasts = battleUnit.remainingTurnActions.remainingCasts,
            maximumCasts = MAX_CASTS,
        )
        // Health
        healthBarView.display(
            remaining = battleUnit.remainingHealthPoints,
            maximum = unit.healthPoints,
        )
        // Mana
        manaBarView.display(
            remaining = battleUnit.remainingManaPoints,
            maximum = unit.manaPoints,
        )
        // Abilities
        if (interactive) {
            abilities.take(abilityButtons.size).forEachIndexed { index, availability ->
                abilityButtons[index].display(availability)
            }
        } else {
            // Inspected units can't be commanded: show their abilities as plain icons, never as buttons.
            battleUnit.abilityCooldowns.keys.forEachIndexed { index, abilityId ->
                readOnlyAbilities.image(sprites.ability(abilityId)) {
                    smoothing = false
                    scale = PIXEL_SCALE.toDouble()
                    x = index * ABILITY_BUTTON_SIZE + (ABILITY_BUTTON_SIZE - ART_SIZE * PIXEL_SCALE) / 2
                    y = (ABILITY_BUTTON_SIZE - ART_SIZE * PIXEL_SCALE) / 2
                }
            }
        }
        // Statuses
        val onTurnStarted =
            battleUnit.ongoingEffects.onTurnStarted.distinct().map { effectId ->
                StatusListView.Status(effectId, battleUnit.ongoingEffects.onTurnStartedTurnsLeft[effectId] ?: 0)
            }
        val onDefeated = battleUnit.ongoingEffects.onDefeatedEffects.map { effectId -> StatusListView.Status(effectId, null) }
        statusListView.display(onTurnStarted + onDefeated)
        visible = true
    }

    fun displayAbilitySelected(index: Int) {
        abilityButtons.forEach(AbilityButtonView::unselect)
        abilityButtons[index].select()
    }

    /** Shows the selected ability's name, cost and summary. An empty text clears the line. */
    fun displayAbilityLine(text: String) {
        persistentAbilityLine = text
        transientMessageSecondsLeft = 0.0
        abilityLine.text = text
    }

    /** Shows [text] for about 1.5 s, then goes back to the ability line. Any later interaction replaces it. */
    fun displayAbilityMessage(text: String) {
        abilityLine.text = text
        transientMessageSecondsLeft = MESSAGE_SECONDS
    }

    fun hide() {
        visible = false
    }
}
