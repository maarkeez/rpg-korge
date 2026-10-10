package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import com.mkz.rpg.unit.domain.Unit
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/**
 * The sheet shown while a cast is previewed: the caster with its mana before and after, the cooldown the ability
 * will set, and one line per affected unit or tile. Lines past [MAX_LINES] collapse into a "+N more" line.
 */
class AttackPreviewView(
    sprites: SpriteRegistry = SpriteRegistry(),
) : Container() {
    companion object {
        const val PREVIEW_LINE = "PREVIEW_LINE"
        const val COOLDOWN_LINE = "COOLDOWN_LINE"
        const val MAX_LINES = 4
        const val LINE_HEIGHT = 17.0
        private const val WIDTH = 390.0
        private const val LINES_Y = 108.0
    }

    private lateinit var casterUnitNameView: UnitNameView
    private lateinit var casterUnitPortraitView: UnitPortraitView
    private lateinit var casterHealthBarView: HealthBarView
    private lateinit var casterManaBarView: ManaBarPreviewView
    private lateinit var cooldownText: UIText
    private val linesContainer = Container()
    private var currentLines: List<CastPreviewSummary.Line> = emptyList()
    private var expanded = false

    suspend fun loadAssets() {
        casterUnitPortraitView.loadAssets()
    }

    private val layout =
        uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                casterUnitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), sprites)
                addChild(casterUnitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    casterUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(casterUnitNameView)

                    uiSpacing(Size(0, 15))
                    casterHealthBarView = HealthBarView(Size(281.5, 16))
                    addChild(casterHealthBarView)

                    uiSpacing(Size(0, 4))
                    casterManaBarView = ManaBarPreviewView(Size(281.5, 16))
                    addChild(casterManaBarView)

                    uiSpacing(Size(0, 4))
                    cooldownText =
                        uiText("", size = Size(width = 281.5, height = LINE_HEIGHT)) {
                            name = COOLDOWN_LINE
                            styles.textColor = UiPalette.textMuted
                            styles.textAlignment = TextAlignment.MIDDLE_LEFT
                            styles.textSize = 12.0
                        }
                }
            }
        }

    init {
        visible = false
        addChild(layout)
        linesContainer.y = LINES_Y
        addChild(linesContainer)
    }

    /** The preview lines currently shown, for tests. */
    val displayedLines: List<String> get() = linesContainer.children.filterIsInstance<UIText>().map { it.text }

    val cooldownLineText: String get() = cooldownText.text

    fun display(
        casterBattleUnit: BattleUnit.Dto,
        casterUnit: Unit.Dto,
        manaAfter: Int,
        cooldownAfter: Int,
        lines: List<CastPreviewSummary.Line>,
    ) {
        casterUnitPortraitView.display(casterBattleUnit.unitId)
        casterUnitNameView.display(unitName = casterUnit.name)
        casterHealthBarView.display(
            remaining = casterBattleUnit.remainingHealthPoints,
            maximum = casterUnit.healthPoints,
        )
        casterManaBarView.display(
            remainingBefore = casterBattleUnit.remainingManaPoints,
            remainingAfter = manaAfter,
            maximum = casterUnit.manaPoints,
        )
        cooldownText.text = if (cooldownAfter > 0) "Cooldown: $cooldownAfter ${if (cooldownAfter == 1) "turn" else "turns"}" else "No cooldown"
        expanded = false
        displayLines(lines)
        visible = true
    }

    private fun displayLines(lines: List<CastPreviewSummary.Line>) {
        currentLines = lines
        linesContainer.removeChildren()
        val collapsed = lines.size > MAX_LINES && !expanded
        val shown = if (collapsed) lines.take(MAX_LINES - 1) else lines
        shown.forEachIndexed { index, line -> addLine(index, line.text, colorOf(line.kind)) }
        if (collapsed) {
            addLine(shown.size, "+${lines.size - shown.size} more", UiPalette.textMuted).onClick {
                expanded = true
                displayLines(currentLines)
            }
        }
    }

    private fun addLine(
        index: Int,
        text: String,
        color: RGBA,
    ): UIText =
        UIText(text, size = Size(width = WIDTH, height = LINE_HEIGHT)).also {
            it.name = PREVIEW_LINE
            it.y = index * LINE_HEIGHT
            it.styles.textColor = color
            it.styles.textAlignment = TextAlignment.MIDDLE_LEFT
            it.styles.textSize = 12.0
            linesContainer.addChild(it)
        }

    private fun colorOf(kind: CastPreviewSummary.Line.Kind) =
        when (kind) {
            CastPreviewSummary.Line.Kind.LETHAL_TARGET -> UiPalette.danger
            CastPreviewSummary.Line.Kind.CONDITIONAL -> UiPalette.conditional
            CastPreviewSummary.Line.Kind.UNSUPPORTED -> UiPalette.textMuted
            CastPreviewSummary.Line.Kind.TARGET,
            CastPreviewSummary.Line.Kind.TILE,
            -> UiPalette.textPrimary
        }

    fun hide() {
        visible = false
    }
}
