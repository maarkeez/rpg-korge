package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.NpcDialog
import com.mkz.rpg.screen.adapters.presentation.NpcDialogView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [NpcDialogView], at the size the game displays it. Tapping the dialog, or one of its choices, logs it. */
object NpcDialogViewStories : StoryBinding {
    private val SIZE = Size(390, 300)
    private const val DEFAULT_NPC = "elder"
    private const val RECRUITABLE_UNIT = "knight"

    // The option of the portrait controls when no portrait is meant, which no npc nor unit is.
    private const val NONE = "none"

    private const val NAME = "name"
    private const val LINE = "line"
    private const val NPC = "npc"
    private const val UNIT = "unit"
    private const val MERCHANT = "merchant"

    private const val ELDER_LINE = "Long ago a goat chewed up a wizard's spellbook and swallowed its magic."

    private val elder =
        StoryDto(id = "screen-npc-dialog--elder", name = "Elder", description = "An npc saying its line: its portrait on the left, its name on top and its line below it.", args = emptyMap())
    private val longLine =
        StoryDto(
            id = "screen-npc-dialog--long-line",
            name = "Long line",
            description = "A line that wraps over several lines, all of it at once.",
            args =
                mapOf(
                    LINE to
                        ArgDto.text(
                            "$ELDER_LINE The Goat Wizard now rules the lair down south. " +
                                "Beat it, and its bees may answer your call.",
                        ),
                ),
        )
    private val merchant =
        StoryDto(
            id = "screen-npc-dialog--merchant",
            name = "Merchant",
            description = "A merchant offers Buy, Sell and Leave along the bottom, and tapping the dialog does not close it.",
            args = mapOf(NAME to ArgDto.text("Merchant"), LINE to ArgDto.text("Fine wares, for fine adventurers!"), NPC to ArgDto.text("merchant"), MERCHANT to ArgDto.boolean(true)),
        )
    private val recruitable =
        StoryDto(
            id = "screen-npc-dialog--recruitable",
            name = "Recruitable",
            description = "An npc that joins the party takes the portrait of its unit.",
            args = mapOf(NAME to ArgDto.text("Knight"), LINE to ArgDto.text("Take me with you, and I will protect the party."), NPC to ArgDto.text(NONE), UNIT to ArgDto.text(RECRUITABLE_UNIT)),
        )
    private val noPortrait =
        StoryDto(
            id = "screen-npc-dialog--no-portrait",
            name = "No portrait",
            description = "An npc with no portrait: its name and its line alone.",
            args = mapOf(NAME to ArgDto.text("Ghost"), LINE to ArgDto.text("Boo."), NPC to ArgDto.text(NONE)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-npc-dialog",
            group = "Screen",
            title = "NPC dialog",
            description =
                "What an npc says when the party talks to it, in place of the pad. " +
                    "npc: the npc whose portrait is drawn. unit: the unit of a recruitable npc, whose portrait wins over the npc's. merchant: its choices.",
            controls =
                listOf(
                    ControlDto.text(NAME),
                    ControlDto.text(LINE),
                    ControlDto.pick(NPC, listOf(NONE) + context.npcIds()),
                    ControlDto.pick(UNIT, listOf(NONE) + context.unitIds()),
                    ControlDto.boolean(MERCHANT),
                ),
            args =
                mapOf(
                    NAME to ArgDto.text("Elder"),
                    LINE to ArgDto.text(ELDER_LINE),
                    NPC to ArgDto.text(DEFAULT_NPC),
                    UNIT to ArgDto.text(NONE),
                    MERCHANT to ArgDto.boolean(false),
                ),
            stories = listOf(elder, longLine, merchant, recruitable, noPortrait),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val texts = context.texts
        val choices =
            if (args.boolean(MERCHANT)) {
                listOf(UiText.NPC_DIALOG_BUY, UiText.NPC_DIALOG_SELL, UiText.NPC_DIALOG_LEAVE).map { text ->
                    val label = texts[text]
                    NpcDialogView.Choice(label) { context.action("onSelected", label) }
                }
            } else {
                emptyList()
            }
        return NpcDialogView(SIZE, context.resourcesLoader).apply {
            setOnTapped { context.action("onTapped") }
            display(
                npc = NpcDialog(npcId = args.text(NPC), unitId = args.text(UNIT).takeIf { unitId -> unitId != NONE }, name = args.text(NAME), line = args.text(LINE)),
                choices = choices,
            )
            // The presenter shows it when the party talks.
            visible = true
        }
    }
}
