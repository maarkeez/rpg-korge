package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Effect
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Reason
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Targeting
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [AbilityInfoView], at the width the battle unit info displays it. The abilities are plain data, as in
 * resources/ability and resources/effect, since the Storybook can not read the game data.
 */
object AbilityInfoViewStories : StoryBinding {
    private const val COOLDOWN_LIMIT = 9
    private const val MANA_LIMIT = 20

    private const val ABILITY = "ability"
    private const val COOLDOWN_TURNS_LEFT = "cooldownTurnsLeft"
    private const val REMAINING_MANA = "remainingMana"

    private val abilities: Map<String, AbilityInfo> =
        listOf(
            AbilityInfo("sword", manaCost = 0, cooldown = 0, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(10))),
            AbilityInfo("skull", manaCost = 10, cooldown = 0, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(10), Effect.OnDefeatedPoisonAllies(damage = 3, turns = 5))),
            AbilityInfo("bat", manaCost = 10, cooldown = 0, Targeting.VACANT_TILE_ADJACENT_TO_SELF, listOf(Effect.Summon(unitId = "bat", turns = 5, healthPoints = 10))),
            AbilityInfo("mending", manaCost = 10, cooldown = 2, Targeting.SELF, listOf(Effect.HealSelf(15), Effect.HealNearbyAllies(15))),
            AbilityInfo("teleport", manaCost = 5, cooldown = 1, Targeting.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, listOf(Effect.Teleport)),
            AbilityInfo("cleave", manaCost = 5, cooldown = 1, Targeting.ALL_ADJACENT_ENEMIES, listOf(Effect.Damage(15))),
            AbilityInfo("venom-strike", manaCost = 5, cooldown = 1, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(5), Effect.DamageEachTurn(damage = 3, turns = 5))),
        ).associateBy(AbilityInfo::abilityId)

    private val sword = StoryDto(id = "screen-ability-info--sword", name = "Sword", description = "One effect, free and with no cooldown.", args = emptyMap())
    private val skull =
        StoryDto(
            id = "screen-ability-info--skull",
            name = "Skull",
            description = "An effect applied when the target is defeated, expanded one level.",
            args = mapOf(ABILITY to ArgDto.text("skull")),
        )
    private val bat = StoryDto(id = "screen-ability-info--bat", name = "Bat", description = "A summon, with its turns and health.", args = mapOf(ABILITY to ArgDto.text("bat")))
    private val mending =
        StoryDto(
            id = "screen-ability-info--mending",
            name = "Mending",
            description = "A heal of the caster and of its adjacent allies, marked green in the diagram.",
            args = mapOf(ABILITY to ArgDto.text("mending")),
        )
    private val inCooldown =
        StoryDto(
            id = "screen-ability-info--in-cooldown",
            name = "In cooldown",
            description = "Mending, cast last turn: the turns left, in orange, under its name.",
            args = mapOf(ABILITY to ArgDto.text("mending"), COOLDOWN_TURNS_LEFT to ArgDto.int(2)),
        )
    private val notEnoughMana =
        StoryDto(
            id = "screen-ability-info--not-enough-mana",
            name = "Not enough mana",
            description = "Bat, with 4 mana left: the mana missing, in blue, under its name.",
            args = mapOf(ABILITY to ArgDto.text("bat"), REMAINING_MANA to ArgDto.int(4)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-ability-info",
            group = "Screen",
            title = "Ability info",
            description = "What the selected ability does, docked under the ability buttons. A cooldown wins over a lack of mana, as on the button.",
            controls =
                listOf(
                    ControlDto.pick(ABILITY, abilities.keys.toList()),
                    ControlDto.int(COOLDOWN_TURNS_LEFT, minimum = 0, maximum = COOLDOWN_LIMIT),
                    ControlDto.int(REMAINING_MANA, minimum = 0, maximum = MANA_LIMIT),
                ),
            args = mapOf(ABILITY to ArgDto.text("sword"), COOLDOWN_TURNS_LEFT to ArgDto.int(0), REMAINING_MANA to ArgDto.int(MANA_LIMIT)),
            stories = listOf(sword, skull, bat, mending, inCooldown, notEnoughMana),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        AbilityInfoView(AbilityInfoView.WIDTH, context.resourcesLoader, context.texts).apply {
            display(abilityInfo(args))
        }

    /** The ability of the args, with the reason it can not be cast now, if any. */
    internal fun abilityInfo(args: Args): AbilityInfo {
        val ability = abilities.getValue(args.text(ABILITY))
        val cooldownTurnsLeft = args.int(COOLDOWN_TURNS_LEFT)
        val remainingMana = args.int(REMAINING_MANA)
        val reason =
            when {
                cooldownTurnsLeft > 0 -> Reason.Cooldown(cooldownTurnsLeft)
                remainingMana < ability.manaCost -> Reason.NotEnoughMana(cost = ability.manaCost, remainingMana = remainingMana)
                else -> null
            }
        return ability.copy(reason = reason)
    }
}
