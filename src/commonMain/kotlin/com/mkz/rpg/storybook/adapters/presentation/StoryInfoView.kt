package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.DefaultTtfFontAsBitmap
import korlibs.image.text.RichTextData
import korlibs.image.text.TextAlignment
import korlibs.korge.view.Container
import korlibs.korge.view.TextBlock
import korlibs.korge.view.position
import korlibs.korge.view.textBlock
import korlibs.math.geom.Size

/**
 * What the story on show is, for whoever browses the catalogue: its component, e.g.: `Screen / Health bar`, with its
 * description, and the story, e.g.: `Full`, with its description.
 */
open class StoryInfoView : Container() {
    companion object {
        const val WIDTH = 480.0
        const val HEIGHT = 236.0
        val SECONDARY_COLOR = RGBA(0xBB, 0xBB, 0xBB)
    }

    internal val componentText: TextBlock = textLine(y = 0.0, height = 32.0)
    internal val componentDescriptionText: TextBlock = textLine(y = 40.0, height = 72.0)
    internal val storyNameText: TextBlock = textLine(y = 128.0, height = 28.0)
    internal val storyDescriptionText: TextBlock = textLine(y = 164.0, height = 72.0)

    open fun display(
        component: ComponentDto,
        story: StoryDto,
    ) {
        componentText.text = richText("${component.group} / ${component.title}", textSize = 26.0, color = Colors.WHITE)
        componentDescriptionText.text = richText(component.description, textSize = 16.0, color = SECONDARY_COLOR)
        storyNameText.text = richText(story.name, textSize = 22.0, color = SidebarView.SELECTED_COLOR)
        storyDescriptionText.text = richText(story.description, textSize = 16.0, color = SECONDARY_COLOR)
    }

    private fun textLine(
        y: Double,
        height: Double,
    ): TextBlock =
        textBlock(richText(""), align = TextAlignment.TOP_LEFT, size = Size(WIDTH, height)) {
            position(0.0, y)
            wordWrap = true
        }

    private fun richText(
        value: String,
        textSize: Double = 16.0,
        color: RGBA = Colors.WHITE,
    ): RichTextData = RichTextData(value, font = DefaultTtfFontAsBitmap, textSize = textSize, color = color)
}
