package com.mkz.tessavar.screen.adapters.presentation

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

open class TalkView(
    size: Size,
    private val resourcesLoader: GameSceneResourcesLoader,
) : UIContainer(size) {
    companion object {
        const val PORTRAIT_SCALE = 3.0
        const val PORTRAIT_SIZE = 32 * PORTRAIT_SCALE
        const val PADDING = 12.0
        const val NAME_TEXT_SIZE = 20.0
        const val NAME_HEIGHT = 28.0
        const val LINE_TEXT_SIZE = 18.0

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

    open fun display(npc: TalkInfo) {
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
    }

    private fun text(
        value: String,
        textSize: Double = LINE_TEXT_SIZE,
        color: RGBA = LINE_COLOR,
    ): RichTextData = RichTextData(value, font = DefaultTtfFontAsBitmap, textSize = textSize, color = color)
}
