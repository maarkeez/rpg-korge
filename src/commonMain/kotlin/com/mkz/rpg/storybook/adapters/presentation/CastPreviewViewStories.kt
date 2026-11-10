package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.CastPreviewView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object CastPreviewViewStories : StoryBinding {
    private const val DEFAULT_ABILITY = "mushroom"
    private const val DEFAULT_CASTER = "hero"
    private const val DEFAULT_RECIPIENT = "goat-wizard"
    private const val MAXIMUM_HEALTH = 30
    private const val CASTER_HEALTH = 20
    private const val CASTER_MANA = 20
    private const val RECIPIENT_MANA = 10
    private const val RECIPIENTS_LIMIT = 4
    private const val EFFECTS_LIMIT = 4

    private const val ABILITY = "ability"
    private const val CASTER = "caster"
    private const val RECIPIENT = "recipient"
    private const val RECIPIENTS = "recipients"
    private const val MANA_COST = "manaCost"
    private const val CASTER_HEALTH_AFTER = "casterHealthAfter"
    private const val RECIPIENT_HEALTH_AFTER = "recipientHealthAfter"
    private const val EFFECTS = "effects"
    private const val NEW_EFFECTS = "newEffects"

    private val oneEnemy =
        StoryDto(
            id = "screen-cast-preview--one-enemy",
            name = "One enemy",
            description = "An ability cast on an enemy: the ability and its caster on a line with the mana it spends, then the enemy's row with the health it would lose.",
            args = emptyMap(),
        )
    private val selfCast =
        StoryDto(
            id = "screen-cast-preview--self-cast",
            name = "Self cast",
            description = "An ability cast on the caster alone, e.g.: a heal: only its row, with the health it would gain in green.",
            args = mapOf(ABILITY to ValueDto.text("heal"), RECIPIENTS to ValueDto.int(0), CASTER_HEALTH_AFTER to ValueDto.int(25)),
        )
    private val severalEnemies =
        StoryDto(
            id = "screen-cast-preview--several-enemies",
            name = "Several enemies",
            description = "Three enemies, a row each, all in sight: there is no scroll.",
            args = mapOf(RECIPIENTS to ValueDto.int(3)),
        )
    private val defeat =
        StoryDto(
            id = "screen-cast-preview--defeat",
            name = "Defeat",
            description = "An enemy that the cast would defeat: it reads KO, its portrait ringed grey.",
            args = mapOf(RECIPIENT_HEALTH_AFTER to ValueDto.int(0)),
        )
    private val newEffects =
        StoryDto(
            id = "screen-cast-preview--new-effects",
            name = "New effects",
            description = "The effects the cast would apply, after the ones the battle units have already.",
            args = mapOf(NEW_EFFECTS to ValueDto.int(2)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-cast-preview",
            group = "Screen",
            title = "Cast preview",
            description =
                "The cast waiting to be confirmed, in the HUD, as a forecast: the ability and its caster, then a row per battle unit it changes. " +
                    "effects: the ones every battle unit has already. newEffects: the ones the cast would apply to each.",
            controls =
                listOf(
                    ControlDto.pick(ABILITY, context.abilityIds()),
                    ControlDto.pick(CASTER, context.unitIds()),
                    ControlDto.pick(RECIPIENT, context.unitIds()),
                    ControlDto.int(RECIPIENTS, minimum = 0, maximum = RECIPIENTS_LIMIT),
                    ControlDto.int(MANA_COST, minimum = 0, maximum = CASTER_MANA),
                    ControlDto.int(CASTER_HEALTH_AFTER, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(RECIPIENT_HEALTH_AFTER, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                    ControlDto.int(NEW_EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                ),
            args =
                mapOf(
                    ABILITY to ValueDto.text(DEFAULT_ABILITY),
                    CASTER to ValueDto.text(DEFAULT_CASTER),
                    RECIPIENT to ValueDto.text(DEFAULT_RECIPIENT),
                    RECIPIENTS to ValueDto.int(1),
                    MANA_COST to ValueDto.int(10),
                    CASTER_HEALTH_AFTER to ValueDto.int(CASTER_HEALTH),
                    RECIPIENT_HEALTH_AFTER to ValueDto.int(18),
                    EFFECTS to ValueDto.int(0),
                    NEW_EFFECTS to ValueDto.int(0),
                ),
            stories = listOf(oneEnemy, selfCast, severalEnemies, defeat, newEffects),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val effectIds = context.effectIds()
        val effects = List(args.int(EFFECTS)) { index -> effectIds[index % effectIds.size] }
        val newEffects = List(args.int(NEW_EFFECTS)) { index -> effectIds[index % effectIds.size] }
        val caster =
            BattleUnitInfo(
                unitId = args.text(CASTER),
                remainingHealth = CASTER_HEALTH,
                maximumHealth = MAXIMUM_HEALTH,
                remainingMana = CASTER_MANA,
                maximumMana = CASTER_MANA,
                effectIds = effects,
                isHumanPlayers = true,
            )
        val recipient =
            CastPreviewView.Recipient(
                battleUnit =
                    BattleUnitInfo(
                        unitId = args.text(RECIPIENT),
                        remainingHealth = MAXIMUM_HEALTH,
                        maximumHealth = MAXIMUM_HEALTH,
                        remainingMana = RECIPIENT_MANA,
                        maximumMana = RECIPIENT_MANA,
                        effectIds = effects,
                        isHumanPlayers = false,
                    ),
                remainingHealth = args.int(RECIPIENT_HEALTH_AFTER),
                newEffectIds = newEffects,
            )
        return CastPreviewView(context.resourcesLoader, context.messages).apply {
            display(
                abilityId = args.text(ABILITY),
                casterBattleUnit = caster,
                manaCost = args.int(MANA_COST),
                recipients = List(args.int(RECIPIENTS)) { recipient },
                casterRemainingHealth = args.int(CASTER_HEALTH_AFTER),
                casterNewEffectIds = newEffects,
            )
        }
    }
}
