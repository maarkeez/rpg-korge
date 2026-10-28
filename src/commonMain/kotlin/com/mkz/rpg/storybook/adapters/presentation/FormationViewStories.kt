package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.FormationView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [FormationView], built with its real [com.mkz.rpg.screen.adapters.presentation.FormationRowView]s, with the
 * units that have a portrait, in alphabetical order, as many times as needed. Tapping a row selects it, as the formation
 * presenter does, and the hint tells what the next tap does.
 */
object FormationViewStories : StoryBinding {
    private const val MAXIMUM_HEALTH = 20
    private const val MAXIMUM_MANA = 10

    // The option of the selected row control when no row is selected, which no row index is.
    private const val NO_ROW = "none"

    private const val MEMBERS = "members"
    private const val SELECTED_ROW = "selectedRow"
    private const val HURT = "hurt"

    private val party = StoryDto(id = "screen-formation--party", name = "Party", description = "Three party members, the first one the leader: the hint asks to tap one.", args = emptyMap())
    private val rowSelected =
        StoryDto(
            id = "screen-formation--row-selected",
            name = "Row selected",
            description = "A row tapped, in blue: the hint asks to tap another one to swap them.",
            args =
                mapOf(
                    SELECTED_ROW to ArgDto.text("1"),
                ),
        )
    private val fullParty =
        StoryDto(
            id = "screen-formation--full-party",
            name = "Full party",
            description = "As many party members as rows fit in place of the pad.",
            args =
                mapOf(
                    MEMBERS to ArgDto.int(FormationView.ROWS),
                ),
        )
    private val hurtParty =
        StoryDto(id = "screen-formation--hurt-party", name = "Hurt party", description = "Party members who lost health and mana.", args = mapOf(HURT to ArgDto.boolean(true)))
    private val alone = StoryDto(id = "screen-formation--alone", name = "Alone", description = "A party of one: the leader only.", args = mapOf(MEMBERS to ArgDto.int(1)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-formation",
            group = "Screen",
            title = "Formation",
            description = "Where the pad is, while the player sets the party order: a row per party member, a hint, and Close. Tap a row, then another one, to swap them.",
            controls =
                listOf(
                    ControlDto.int(MEMBERS, minimum = 1, maximum = FormationView.ROWS),
                    ControlDto.pick(SELECTED_ROW, listOf(NO_ROW) + (0 until FormationView.ROWS).map { index -> index.toString() }),
                    ControlDto.boolean(HURT),
                ),
            args = mapOf(MEMBERS to ArgDto.int(3), SELECTED_ROW to ArgDto.text(NO_ROW), HURT to ArgDto.boolean(false)),
            stories = listOf(party, rowSelected, fullParty, hurtParty, alone),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val unitIds = context.unitIds()
        val members = args.int(MEMBERS)
        // A selected row of no member selects none.
        val selectedRow = args.text(SELECTED_ROW).toIntOrNull()?.takeIf { index -> index < members }
        val isHurt = args.boolean(HURT)
        val rows =
            List(members) { index ->
                val unitId = unitIds[index % unitIds.size]
                FormationView.Row(
                    unitId = unitId,
                    name = context.texts.unitName(unitId),
                    remainingHealth = if (isHurt) MAXIMUM_HEALTH / (index + 2) else MAXIMUM_HEALTH,
                    maximumHealth = MAXIMUM_HEALTH,
                    remainingMana = if (isHurt) MAXIMUM_MANA / (index + 2) else MAXIMUM_MANA,
                    maximumMana = MAXIMUM_MANA,
                    isLeader = index == 0,
                )
            }
        return FormationView(context.resourcesLoader, context.texts).apply {
            setDelegate(
                object : FormationView.Delegate {
                    // Tapping the selected row selects none.
                    override fun formationRowSelected(index: Int) {
                        val selected = index.takeIf { index != selectedRow }
                        context.action("formationRowSelected", index, setArg = SELECTED_ROW to ArgDto.text(selected?.toString() ?: NO_ROW))
                    }

                    override fun formationCloseSelected() = context.action("formationCloseSelected")
                },
            )
            display(rows)
            selectRow(selectedRow)
            displayHint(
                if (selectedRow == null) {
                    context.texts[UiText.FORMATION_TAP_TO_SELECT]
                } else {
                    context.texts[UiText.FORMATION_TAP_TO_SWAP, "name" to rows[selectedRow].name]
                },
            )
        }
    }
}
