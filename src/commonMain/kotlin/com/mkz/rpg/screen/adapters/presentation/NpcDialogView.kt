package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.DefaultTtfFontAsBitmap
import korlibs.image.text.RichTextData
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Image
import korlibs.korge.view.SolidRect
import korlibs.korge.view.TextBlock
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.korge.view.textBlock
import korlibs.math.geom.Size

/**
 * What an npc says when the party talks to it, in place of the virtual pad: its portrait on the left, its name on top
 * and its line below it, wrapping when long, and the choices of the npc along the bottom, if any (e.g.: a merchant's
 * Buy, Sell and Leave). The whole line is displayed at once. Tapping anywhere on it closes it.
 */
open class NpcDialogView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(size) {
    companion object {
        const val PORTRAIT_SCALE = 3.0
        const val PORTRAIT_SIZE = 32 * PORTRAIT_SCALE
        const val PADDING = 12.0
        const val NAME_TEXT_SIZE = 20.0
        const val NAME_HEIGHT = 28.0
        const val LINE_TEXT_SIZE = 18.0
        const val CHOICE_HEIGHT = 32.0

        // From the FamiCube palette.
        private val BACKGROUND_COLOR = RGBA(33, 22, 64)
        private val NAME_COLOR = RGBA(255, 231, 55)
        private val LINE_COLOR = RGBA(255, 255, 255)
    }

    /** A button under the line, e.g.: Buy. */
    data class Choice(
        val label: String,
        val onSelected: () -> Unit,
    )

    private var onTapped: (() -> Unit)? = null

    private val dialogWidth = size.width
    private val textX = PADDING + PORTRAIT_SIZE + PADDING
    private val textWidth = size.width - textX - PADDING
    private val choicesY = size.height - PADDING - CHOICE_HEIGHT

    internal val background: SolidRect = solidRect(size, BACKGROUND_COLOR)
    internal var portrait: Image? = null
        private set
    internal val nameText: TextBlock =
        textBlock(text(""), align = TextAlignment.TOP_LEFT, size = Size(textWidth, NAME_HEIGHT)) {
            position(textX, PADDING)
        }

    // The line ends above the choices.
    internal val lineText: TextBlock =
        textBlock(text(""), align = TextAlignment.TOP_LEFT, size = Size(textWidth, choicesY - PADDING * 3 - NAME_HEIGHT)) {
            position(textX, PADDING * 2 + NAME_HEIGHT)
            wordWrap = true
        }
    internal var choices: List<Choice> = emptyList()
        private set
    internal var choiceButtons: List<UIButton> = emptyList()
        private set

    init {
        visible = false
        onClick { tap() }
    }

    open fun setOnTapped(listener: () -> Unit) {
        onTapped = listener
    }

    internal fun tap() {
        onTapped?.invoke()
    }

    /**
     * Displays the npc saying its line, with its portrait: the one of its unit for a recruitable npc. Its portrait is
     * left out when it has none. The [choices] share the width of the dialog, in their order.
     */
    open fun display(
        npc: NpcDialog,
        choices: List<Choice> = emptyList(),
    ) {
        portrait?.removeFromParent()
        portrait =
            resourcesLoader.bitmapOrNull(npc.unitId?.let { unitId -> "unit/${unitId}_portrait.png" } ?: "npc/${npc.npcId}_portrait.png")?.let { portraitBitmap ->
                image(portraitBitmap) {
                    smoothing = false
                    scale = PORTRAIT_SCALE
                    position(PADDING, PADDING)
                }
            }
        nameText.text = text(npc.name, textSize = NAME_TEXT_SIZE, color = NAME_COLOR)
        lineText.text = text(npc.line, textSize = LINE_TEXT_SIZE, color = LINE_COLOR)
        displayChoices(choices)
    }

    private fun displayChoices(choices: List<Choice>) {
        this.choices = choices
        choiceButtons.forEach { button -> button.removeFromParent() }
        val choiceWidth = (dialogWidth - PADDING * (choices.size + 1)) / choices.size.coerceAtLeast(1)
        choiceButtons =
            choices.mapIndexed { index, choice ->
                uiButton(choice.label, size = Size(choiceWidth, CHOICE_HEIGHT)) {
                    position(PADDING + index * (choiceWidth + PADDING), choicesY)
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = RGBA(85, 85, 85)
                    background.borderColor = Colors.LIGHTGRAY
                    onClick { choice.onSelected() }
                }
            }
    }

    private fun text(
        value: String,
        textSize: Double = LINE_TEXT_SIZE,
        color: RGBA = LINE_COLOR,
    ): RichTextData = RichTextData(value, font = DefaultTtfFontAsBitmap, textSize = textSize, color = color)
}
