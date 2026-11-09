package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TalkInfo
import com.mkz.rpg.screen.adapters.presentation.TalkView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [TalkView], at the size the game displays it. Tapping the talk panel records it. */
object TalkViewStories : StoryBinding {
    private val SIZE = Size(390, 300)
    private const val DEFAULT_NPC = "elder"
    private const val RECRUITABLE_UNIT = "knight"

    // The option of the portrait controls when no portrait is meant, which no npc nor unit is.
    private const val NONE = "none"

    private const val NAME = "name"
    private const val LINE = "line"
    private const val NPC = "npc"
    private const val UNIT = "unit"

    private const val ELDER_LINE = "Long ago a goat chewed up a wizard's spellbook and swallowed its magic."

    private val elder =
        StoryDto(id = "screen-talk-panel--elder", name = "Elder", description = "An npc saying its line: its portrait on the left, its name on top and its line below it.", args = emptyMap())
    private val longLine =
        StoryDto(
            id = "screen-talk-panel--long-line",
            name = "Long line",
            description = "A line that wraps over several lines, all of it at once.",
            args =
                mapOf(
                    LINE to
                        ValueDto.text(
                            "$ELDER_LINE The Goat Wizard now rules the lair down south. " +
                                "Beat it, and its bees may answer your call.",
                        ),
                ),
        )
    private val merchant =
        StoryDto(
            id = "screen-talk-panel--merchant",
            name = "Merchant",
            description = "A merchant, whose line has the whole panel: its Leave, Sell and Buy are in the row of the call to action.",
            args = mapOf(NAME to ValueDto.text("Merchant"), LINE to ValueDto.text("Fine wares, for fine adventurers!"), NPC to ValueDto.text("merchant")),
        )
    private val recruitable =
        StoryDto(
            id = "screen-talk-panel--recruitable",
            name = "Recruitable",
            description = "An npc that joins the party takes the portrait of its unit.",
            args =
                mapOf(
                    NAME to ValueDto.text("Knight"),
                    LINE to ValueDto.text("Take me with you, and I will protect the party."),
                    NPC to ValueDto.text(NONE),
                    UNIT to ValueDto.text(RECRUITABLE_UNIT),
                ),
        )
    private val noPortrait =
        StoryDto(
            id = "screen-talk-panel--no-portrait",
            name = "No portrait",
            description = "An npc with no portrait: its name and its line alone.",
            args = mapOf(NAME to ValueDto.text("Ghost"), LINE to ValueDto.text("Boo."), NPC to ValueDto.text(NONE)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-talk-panel",
            group = "Screen",
            title = "Talk panel",
            description =
                "What an npc says when the party talks to it, in place of the pad. " +
                    "npc: the npc whose portrait is drawn. unit: the unit of a recruitable npc, whose portrait wins over the npc's.",
            controls =
                listOf(
                    ControlDto.text(NAME),
                    ControlDto.text(LINE),
                    ControlDto.pick(NPC, listOf(NONE) + context.npcIds()),
                    ControlDto.pick(UNIT, listOf(NONE) + context.unitIds()),
                ),
            args =
                mapOf(
                    NAME to ValueDto.text("Elder"),
                    LINE to ValueDto.text(ELDER_LINE),
                    NPC to ValueDto.text(DEFAULT_NPC),
                    UNIT to ValueDto.text(NONE),
                ),
            stories = listOf(elder, longLine, merchant, recruitable, noPortrait),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        TalkView(SIZE, context.resourcesLoader).apply {
            setOnTapped { context.action("onTapped") }
            display(TalkInfo(npcId = args.text(NPC), unitId = args.text(UNIT).takeIf { unitId -> unitId != NONE }, name = args.text(NAME), line = args.text(LINE)))
            // The presenter shows it when the party talks.
            visible = true
        }
}
