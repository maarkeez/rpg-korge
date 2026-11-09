package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.CastPreviewView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [CastPreviewView], built with its real children. The caster has 20 of its 30 health points and all
 * its 20 mana points before the cast, and each receiver 30 of 30 health points and 10 mana points. Every row is in
 * sight, as the game shows them: at most the caster and its 4 adjacent tiles.
 */
object CastPreviewViewStories : StoryBinding {
    private const val DEFAULT_ABILITY = "mushroom"
    private const val DEFAULT_CASTER = "hero"
    private const val DEFAULT_RECEIVER = "goat-wizard"
    private const val MAXIMUM_HEALTH = 30
    private const val CASTER_HEALTH = 20
    private const val CASTER_MANA = 20
    private const val RECEIVER_MANA = 10
    private const val RECEIVERS_LIMIT = 4
    private const val EFFECTS_LIMIT = 4

    private const val ABILITY = "ability"
    private const val CASTER = "caster"
    private const val RECEIVER = "receiver"
    private const val RECEIVERS = "receivers"
    private const val MANA_COST = "manaCost"
    private const val CASTER_HEALTH_AFTER = "casterHealthAfter"
    private const val RECEIVER_HEALTH_AFTER = "receiverHealthAfter"
    private const val EFFECTS = "effects"
    private const val NEW_EFFECTS = "newEffects"

    private val oneEnemy =
        StoryDto(
            id = "screen-attack-preview--one-enemy",
            name = "One enemy",
            description = "An ability cast on an enemy: the ability and its caster on a line with the mana it spends, then the enemy's row with the health it would lose.",
            args = emptyMap(),
        )
    private val selfCast =
        StoryDto(
            id = "screen-attack-preview--self-cast",
            name = "Self cast",
            description = "An ability cast on the caster alone, e.g.: a heal: only its row, with the health it would gain in green.",
            args = mapOf(ABILITY to ArgDto.text("heal"), RECEIVERS to ArgDto.int(0), CASTER_HEALTH_AFTER to ArgDto.int(25)),
        )
    private val severalEnemies =
        StoryDto(
            id = "screen-attack-preview--several-enemies",
            name = "Several enemies",
            description = "Three enemies, a row each, all in sight: there is no scroll.",
            args = mapOf(RECEIVERS to ArgDto.int(3)),
        )
    private val defeat =
        StoryDto(
            id = "screen-attack-preview--defeat",
            name = "Defeat",
            description = "An enemy that the cast would leave with no health: KO, its portrait ringed grey.",
            args = mapOf(RECEIVER_HEALTH_AFTER to ArgDto.int(0)),
        )
    private val newEffects =
        StoryDto(
            id = "screen-attack-preview--new-effects",
            name = "New effects",
            description = "The effects the cast would apply, after the ones the battle units have already.",
            args = mapOf(NEW_EFFECTS to ArgDto.int(2)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-attack-preview",
            group = "Screen",
            title = "Attack preview",
            description =
                "The cast waiting to be confirmed, in the HUD, as a forecast: the ability and its caster, then a row per battle unit it changes. " +
                    "effects: the ones every battle unit has already. newEffects: the ones the cast would apply to each.",
            controls =
                listOf(
                    ControlDto.pick(ABILITY, context.abilityIds()),
                    ControlDto.pick(CASTER, context.unitIds()),
                    ControlDto.pick(RECEIVER, context.unitIds()),
                    ControlDto.int(RECEIVERS, minimum = 0, maximum = RECEIVERS_LIMIT),
                    ControlDto.int(MANA_COST, minimum = 0, maximum = CASTER_MANA),
                    ControlDto.int(CASTER_HEALTH_AFTER, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(RECEIVER_HEALTH_AFTER, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                    ControlDto.int(NEW_EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                ),
            args =
                mapOf(
                    ABILITY to ArgDto.text(DEFAULT_ABILITY),
                    CASTER to ArgDto.text(DEFAULT_CASTER),
                    RECEIVER to ArgDto.text(DEFAULT_RECEIVER),
                    RECEIVERS to ArgDto.int(1),
                    MANA_COST to ArgDto.int(10),
                    CASTER_HEALTH_AFTER to ArgDto.int(CASTER_HEALTH),
                    RECEIVER_HEALTH_AFTER to ArgDto.int(18),
                    EFFECTS to ArgDto.int(0),
                    NEW_EFFECTS to ArgDto.int(0),
                ),
            stories = listOf(oneEnemy, selfCast, severalEnemies, defeat, newEffects),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val effectIds = context.effectIds()
        // The effects with an icon, as many times as needed.
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
        val receiver =
            CastPreviewView.Recipient(
                battleUnit =
                    BattleUnitInfo(
                        unitId = args.text(RECEIVER),
                        remainingHealth = MAXIMUM_HEALTH,
                        maximumHealth = MAXIMUM_HEALTH,
                        remainingMana = RECEIVER_MANA,
                        maximumMana = RECEIVER_MANA,
                        effectIds = effects,
                        isHumanPlayers = false,
                    ),
                remainingHealth = args.int(RECEIVER_HEALTH_AFTER),
                newEffectIds = newEffects,
            )
        return CastPreviewView(context.resourcesLoader, context.messages).apply {
            display(
                abilityId = args.text(ABILITY),
                casterBattleUnit = caster,
                manaCost = args.int(MANA_COST),
                recipients = List(args.int(RECEIVERS)) { receiver },
                casterRemainingHealth = args.int(CASTER_HEALTH_AFTER),
                casterNewEffectIds = newEffects,
            )
        }
    }
}
