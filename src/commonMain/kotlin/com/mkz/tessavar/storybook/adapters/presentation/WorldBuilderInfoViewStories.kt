package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.WorldBuilderInfoView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object WorldBuilderInfoViewStories : StoryBinding {
    private const val SIZE_LIMIT = 128

    private const val ROWS = "rows"
    private const val COLUMNS = "columns"
    private const val MESSAGE_KIND = "messageKind"
    private const val MESSAGE = "message"

    private enum class MessageKind { NONE, ERROR, INFO }

    private val size = StoryDto(id = "screen-world-builder-info--size", name = "Size", description = "The title and the world size in the subtitle.", args = emptyMap())
    private val error =
        StoryDto(
            id = "screen-world-builder-info--error",
            name = "Error",
            description = "Why a tile can not be painted, in red: it pops, stays two seconds and the size comes back.",
            args = mapOf(MESSAGE_KIND to ValueDto.text(MessageKind.ERROR.name), MESSAGE to ValueDto.text("Water can not be next to lava")),
        )
    private val info =
        StoryDto(
            id = "screen-world-builder-info--info",
            name = "Info",
            description = "The world was copied, in blue: it pops, stays two seconds and the size comes back.",
            args = mapOf(MESSAGE_KIND to ValueDto.text(MessageKind.INFO.name)),
        )
    private val largeWorld =
        StoryDto(
            id = "screen-world-builder-info--large-world",
            name = "Large world",
            description = "The largest world, whose size takes the most characters.",
            args = mapOf(ROWS to ValueDto.int(SIZE_LIMIT), COLUMNS to ValueDto.int(SIZE_LIMIT)),
        )
    private val longMessage =
        StoryDto(
            id = "screen-world-builder-info--long-message",
            name = "Long message",
            description = "A message longer than the line, as translations may be.",
            args =
                mapOf(
                    MESSAGE_KIND to ValueDto.text(MessageKind.ERROR.name),
                    MESSAGE to ValueDto.text("The world must be 1 to 128 tiles wide and tall, and the file has a world of more tiles"),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-world-builder-info",
            group = "Screen",
            title = "World builder info",
            description =
                "On top of the world builder: its title, and a subtitle with the world size, which an error or an information replaces for a while. " +
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
                    ROWS to ValueDto.int(16),
                    COLUMNS to ValueDto.int(16),
                    MESSAGE_KIND to ValueDto.text(MessageKind.NONE.name),
                    MESSAGE to ValueDto.text("World copied to the clipboard"),
                ),
            stories = listOf(size, error, info, largeWorld, longMessage),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        WorldBuilderInfoView(context.messages).apply {
            displaySize(rows = args.int(ROWS), columns = args.int(COLUMNS))
            when (MessageKind.valueOf(args.text(MESSAGE_KIND))) {
                MessageKind.NONE -> Unit
                MessageKind.ERROR -> displayError(args.text(MESSAGE))
                MessageKind.INFO -> displayInfo(args.text(MESSAGE))
            }
        }
}
