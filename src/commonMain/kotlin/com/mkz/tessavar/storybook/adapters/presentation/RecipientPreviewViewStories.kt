package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.BattleUnitInfo
import com.mkz.tessavar.screen.adapters.presentation.RecipientPreviewView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object RecipientPreviewViewStories : StoryBinding {
    private const val DEFAULT_UNIT = "goat-wizard"
    private const val MAXIMUM_HEALTH = 30
    private const val MAXIMUM_MANA = 20
    private const val REMAINING_MANA = 10
    private const val EFFECTS_LIMIT = 4

    private const val UNIT = "unit"
    private const val HEALTH_AFTER = "healthAfter"
    private const val EFFECTS = "effects"
    private const val NEW_EFFECTS = "newEffects"

    private val hurt = StoryDto(id = "screen-recipient-preview--hurt", name = "Hurt", description = "A recipient that the cast would hurt: the health it would have left.", args = emptyMap())
    private val unharmed =
        StoryDto(
            id = "screen-recipient-preview--unharmed",
            name = "Unharmed",
            description = "A recipient whose health the cast would not change.",
            args = mapOf(HEALTH_AFTER to ValueDto.int(MAXIMUM_HEALTH)),
        )
    private val defeated =
        StoryDto(
            id = "screen-recipient-preview--defeated",
            name = "Defeated",
            description = "A recipient that the cast would leave with no health.",
            args = mapOf(HEALTH_AFTER to ValueDto.int(0)),
        )
    private val newEffects =
        StoryDto(
            id = "screen-recipient-preview--new-effects",
            name = "New effects",
            description = "The effects the cast would apply, after the ones the recipient has already.",
            args = mapOf(EFFECTS to ValueDto.int(1), NEW_EFFECTS to ValueDto.int(2)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-recipient-preview",
            group = "Screen",
            title = "Recipient preview",
            description =
                "A row of the forecast: a battle unit receiving the cast, with its portrait and name, the health it would lose or gain, its mana and its effects, " +
                    "the new ones blinking.",
            controls =
                listOf(
                    ControlDto.pick(UNIT, context.unitIds()),
                    ControlDto.int(HEALTH_AFTER, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                    ControlDto.int(NEW_EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                ),
            args =
                mapOf(
                    UNIT to ValueDto.text(DEFAULT_UNIT),
                    HEALTH_AFTER to ValueDto.int(18),
                    EFFECTS to ValueDto.int(0),
                    NEW_EFFECTS to ValueDto.int(0),
                ),
            stories = listOf(hurt, unharmed, defeated, newEffects),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val effectIds = context.effectIds()
        val effects = List(args.int(EFFECTS)) { index -> effectIds[index % effectIds.size] }
        val newEffects = List(args.int(NEW_EFFECTS)) { index -> effectIds[index % effectIds.size] }
        return RecipientPreviewView(context.resourcesLoader, context.messages).apply {
            display(
                battleUnit =
                    BattleUnitInfo(
                        unitId = args.text(UNIT),
                        remainingHealth = MAXIMUM_HEALTH,
                        maximumHealth = MAXIMUM_HEALTH,
                        remainingMana = REMAINING_MANA,
                        maximumMana = MAXIMUM_MANA,
                        effectIds = effects,
                        isHumanPlayers = false,
                    ),
                remainingHealth = args.int(HEALTH_AFTER),
                newEffectIds = newEffects,
            )
        }
    }
}
