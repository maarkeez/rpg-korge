package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.MapBuilderInfoView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [MapBuilderInfoView], with the texts of the language global. A message pops in the subtitle for a
 * couple of seconds and the size comes back: changing an arg shows it again.
 */
object MapBuilderInfoViewStories : StoryBinding {
    // The sizes the map builder allows, which are in its domain.
    private const val SIZE_LIMIT = 128

    private const val ROWS = "rows"
    private const val COLUMNS = "columns"
    private const val MESSAGE_KIND = "messageKind"
    private const val MESSAGE = "message"

    /** What the subtitle shows: the map size, or a message over it. */
    private enum class MessageKind { NONE, ERROR, INFO }

    private val size = StoryDto(id = "screen-map-builder-info--size", name = "Size", description = "The title and the map size in the subtitle.", args = emptyMap())
    private val error =
        StoryDto(
            id = "screen-map-builder-info--error",
            name = "Error",
            description = "Why a tile can not be painted, in red: it pops, stays two seconds and the size comes back.",
            args = mapOf(MESSAGE_KIND to ArgDto.text(MessageKind.ERROR.name), MESSAGE to ArgDto.text("Water can not be next to lava")),
        )
    private val info =
        StoryDto(
            id = "screen-map-builder-info--info",
            name = "Info",
            description = "The map was copied, in blue: it pops, stays two seconds and the size comes back.",
            args = mapOf(MESSAGE_KIND to ArgDto.text(MessageKind.INFO.name)),
        )
    private val largeMap =
        StoryDto(
            id = "screen-map-builder-info--large-map",
            name = "Large map",
            description = "The largest map, whose size takes the most characters.",
            args = mapOf(ROWS to ArgDto.int(SIZE_LIMIT), COLUMNS to ArgDto.int(SIZE_LIMIT)),
        )
    private val longMessage =
        StoryDto(
            id = "screen-map-builder-info--long-message",
            name = "Long message",
            description = "A message longer than the line, as translations may be.",
            args =
                mapOf(
                    MESSAGE_KIND to ArgDto.text(MessageKind.ERROR.name),
                    MESSAGE to ArgDto.text("The map must be 1 to 128 tiles wide and tall, and the file has a map of more tiles"),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-map-builder-info",
            group = "Screen",
            title = "Map builder info",
            description =
                "On top of the map builder: its title, and a subtitle with the map size, which an error or an information replaces for a while. " +
                    "messageKind: the message over the size. message: its text.",
            controls =
                listOf(
                    ControlDto.int(ROWS, minimum = 1, maximum = SIZE_LIMIT),
                    ControlDto.int(COLUMNS, minimum = 1, maximum = SIZE_LIMIT),
                    ControlDto.enum(MESSAGE_KIND, MessageKind.entries.map { kind -> kind.name }),
                    ControlDto.text(MESSAGE),
                ),
            args =
                mapOf(
                    ROWS to ArgDto.int(16),
                    COLUMNS to ArgDto.int(16),
                    MESSAGE_KIND to ArgDto.text(MessageKind.NONE.name),
                    MESSAGE to ArgDto.text("Map copied to the clipboard"),
                ),
            stories = listOf(size, error, info, largeMap, longMessage),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        MapBuilderInfoView(context.texts).apply {
            displaySize(rows = args.int(ROWS), columns = args.int(COLUMNS))
            when (MessageKind.valueOf(args.text(MESSAGE_KIND))) {
                MessageKind.NONE -> Unit
                MessageKind.ERROR -> displayError(args.text(MESSAGE))
                MessageKind.INFO -> displayInfo(args.text(MESSAGE))
            }
        }
}
