package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.npc.domain.Npc
import korlibs.image.color.RGBA
import korlibs.image.font.DefaultTtfFontAsBitmap
import korlibs.image.text.RichTextData
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIContainer
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
 * and its line below it, wrapping when long. The whole line is displayed at once. Tapping anywhere on it closes it.
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

        // From the FamiCube palette.
        private val BACKGROUND_COLOR = RGBA(33, 22, 64)
        private val NAME_COLOR = RGBA(255, 231, 55)
        private val LINE_COLOR = RGBA(255, 255, 255)
    }

    private var onTapped: (() -> Unit)? = null

    private val textX = PADDING + PORTRAIT_SIZE + PADDING
    private val textWidth = size.width - textX - PADDING

    internal val background: SolidRect = solidRect(size, BACKGROUND_COLOR)
    internal var portrait: Image? = null
        private set
    internal val nameText: TextBlock =
        textBlock(text(""), align = TextAlignment.TOP_LEFT, size = Size(textWidth, NAME_HEIGHT)) {
            position(textX, PADDING)
        }
    internal val lineText: TextBlock =
        textBlock(text(""), align = TextAlignment.TOP_LEFT, size = Size(textWidth, size.height - PADDING * 3 - NAME_HEIGHT)) {
            position(textX, PADDING * 2 + NAME_HEIGHT)
            wordWrap = true
        }

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
     * left out when it has none.
     */
    open fun display(npc: Npc.Dto) {
        portrait?.removeFromParent()
        portrait =
            resourcesLoader.bitmapOrNull(npc.unitId?.let { unitId -> "unit/${unitId}_portrait.png" } ?: "npc/${npc.id}_portrait.png")?.let { portraitBitmap ->
                image(portraitBitmap) {
                    smoothing = false
                    scale = PORTRAIT_SCALE
                    position(PADDING, PADDING)
                }
            }
        nameText.text = text(npc.name, textSize = NAME_TEXT_SIZE, color = NAME_COLOR)
        lineText.text = text(npc.line, textSize = LINE_TEXT_SIZE, color = LINE_COLOR)
    }

    private fun text(
        value: String,
        textSize: Double = LINE_TEXT_SIZE,
        color: RGBA = LINE_COLOR,
    ): RichTextData = RichTextData(value, font = DefaultTtfFontAsBitmap, textSize = textSize, color = color)
}
