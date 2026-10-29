package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AttackPreviewView
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [AttackPreviewView], built with its real children. The caster has 20 of its 30 health points and all
 * its 20 mana points before the cast, and each receiver 30 of 30 health points and 10 mana points. Dragging the preview
 * up and down scrolls through the receivers, two rows at a time, as the game does.
 */
object AttackPreviewViewStories : StoryBinding {
    private const val DEFAULT_CASTER = "hero"
    private const val DEFAULT_RECEIVER = "goat-wizard"
    private const val MAXIMUM_HEALTH = 30
    private const val CASTER_HEALTH = 20
    private const val CASTER_MANA = 20
    private const val RECEIVER_MANA = 10
    private const val RECEIVERS_LIMIT = 6
    private const val EFFECTS_LIMIT = 4

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
            description = "An ability cast on an enemy: the health it would have left, and the mana the caster would spend.",
            args = emptyMap(),
        )
    private val selfCast =
        StoryDto(
            id = "screen-attack-preview--self-cast",
            name = "Self cast",
            description = "An ability cast on the caster alone, e.g.: a heal: only its row, with the health it would have left.",
            args = mapOf(RECEIVERS to ArgDto.int(0), CASTER_HEALTH_AFTER to ArgDto.int(25)),
        )
    private val severalEnemies =
        StoryDto(
            id = "screen-attack-preview--several-enemies",
            name = "Several enemies",
            description = "The caster and the first enemy are in sight, the others are scrolled to with a drag, each with its counter.",
            args = mapOf(RECEIVERS to ArgDto.int(3)),
        )
    private val defeat =
        StoryDto(
            id = "screen-attack-preview--defeat",
            name = "Defeat",
            description = "An enemy that the cast would leave with no health.",
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
                "The cast waiting to be confirmed, in the HUD: the caster and a row per receiver, with the health, mana and effects they would have after it. " +
                    "effects: the ones every battle unit has already. newEffects: the ones the cast would apply to each.",
            controls =
                listOf(
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
                remainingHealthPoints = CASTER_HEALTH,
                maximumHealthPoints = MAXIMUM_HEALTH,
                remainingManaPoints = CASTER_MANA,
                maximumManaPoints = CASTER_MANA,
                effectIds = effects,
            )
        val receiver =
            AttackPreviewView.Receiver(
                battleUnit =
                    BattleUnitInfo(
                        unitId = args.text(RECEIVER),
                        remainingHealthPoints = MAXIMUM_HEALTH,
                        maximumHealthPoints = MAXIMUM_HEALTH,
                        remainingManaPoints = RECEIVER_MANA,
                        maximumManaPoints = RECEIVER_MANA,
                        effectIds = effects,
                    ),
                remainingHealthPoints = args.int(RECEIVER_HEALTH_AFTER),
                newEffectIds = newEffects,
            )
        return AttackPreviewView(context.resourcesLoader, context.texts).apply {
            display(
                casterBattleUnit = caster,
                manaCost = args.int(MANA_COST),
                receivers = List(args.int(RECEIVERS)) { receiver },
                casterRemainingHealthPoints = args.int(CASTER_HEALTH_AFTER),
                casterNewEffectIds = newEffects,
            )
        }
    }
}
