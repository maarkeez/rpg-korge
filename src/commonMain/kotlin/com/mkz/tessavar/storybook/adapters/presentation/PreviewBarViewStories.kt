package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.PreviewBarView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

object PreviewBarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_VALUE = 30
    private const val VALUE_LIMIT = 999

    private const val PREVIEW_ALPHA = 0x80

    private const val BEFORE = "remainingBefore"
    private const val AFTER = "remainingAfter"
    private const val MAXIMUM = "maximum"
    private const val FILLED_COLOR = "filledColor"
    private const val PREVIEW_COLOR = "previewColor"

    private const val HEALTH_COLOR = "FF375F"
    private const val MANA_COLOR = "0091FF"
    private const val WHITE = "FFFFFF"

    private val unchanged = StoryDto(id = "shared-preview-bar--unchanged", name = "Unchanged", description = "A change that leaves the bar as it is: nothing shows over it.", args = emptyMap())
    private val decreased =
        StoryDto(
            id = "shared-preview-bar--decreased",
            name = "Decreased",
            description = "The bar after a change, over the paler one it had before.",
            args = mapOf(BEFORE to ValueDto.int(MAXIMUM_VALUE), AFTER to ValueDto.int(MAXIMUM_VALUE / 3)),
        )
    private val emptied =
        StoryDto(
            id = "shared-preview-bar--emptied",
            name = "Emptied",
            description = "A change that takes everything the bar has left.",
            args = mapOf(BEFORE to ValueDto.int(MAXIMUM_VALUE / 2), AFTER to ValueDto.int(0)),
        )
    private val otherColors =
        StoryDto(
            id = "shared-preview-bar--other-colors",
            name = "Other colors",
            description = "The colors of the mana bar instead of the health bar's: the bar and its preview fill up with any color.",
            args = mapOf(BEFORE to ValueDto.int(MAXIMUM_VALUE), AFTER to ValueDto.int(MAXIMUM_VALUE / 3), FILLED_COLOR to ValueDto.text(MANA_COLOR)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "shared-preview-bar",
            group = "Shared",
            title = "Preview bar",
            description = "The base of the health and mana preview bars: the value left after a change, over the value before it, to show what the change would do.",
            controls =
                listOf(
                    ControlDto.int(BEFORE, minimum = 0, maximum = VALUE_LIMIT),
                    ControlDto.int(AFTER, minimum = 0, maximum = VALUE_LIMIT),
                    ControlDto.int(MAXIMUM, minimum = 1, maximum = VALUE_LIMIT),
                    ControlDto.color(FILLED_COLOR),
                    ControlDto.color(PREVIEW_COLOR),
                ),
            args =
                mapOf(
                    BEFORE to ValueDto.int(MAXIMUM_VALUE),
                    AFTER to ValueDto.int(MAXIMUM_VALUE),
                    MAXIMUM to ValueDto.int(MAXIMUM_VALUE),
                    FILLED_COLOR to ValueDto.text(HEALTH_COLOR),
                    PREVIEW_COLOR to ValueDto.text(WHITE),
                ),
            stories = listOf(unchanged, decreased, emptied, otherColors),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        PreviewBarView(SIZE, filledColor = args.color(FILLED_COLOR), previewColor = args.color(PREVIEW_COLOR).withA(PREVIEW_ALPHA)).apply {
            display(remainingBefore = args.int(BEFORE), remainingAfter = args.int(AFTER), maximum = args.int(MAXIMUM))
        }
}
