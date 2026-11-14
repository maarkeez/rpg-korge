package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView
import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Effect
import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Reason
import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Targeting
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object AbilityInfoViewStories : StoryBinding {
    private const val COOLDOWN_LIMIT = 9
    private const val MANA_LIMIT = 20

    private const val ABILITY = "ability"
    private const val COOLDOWN_TURNS_LEFT = "cooldownTurnsLeft"
    private const val REMAINING_MANA = "remainingMana"

    private val abilities: Map<String, AbilityInfo> =
        listOf(
            AbilityInfo("sword", manaCost = 0, cooldown = 0, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(10))),
            AbilityInfo("skull", manaCost = 10, cooldown = 0, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(10), Effect.OnDefeatedDamageAdjacentAllies(damage = 3, turns = 5))),
            AbilityInfo("bat", manaCost = 10, cooldown = 0, Targeting.VACANT_TILE_ADJACENT_TO_CASTER, listOf(Effect.Summon(unitId = "bat", turns = 5, baseMaximumHealth = 10))),
            AbilityInfo("mending", manaCost = 10, cooldown = 2, Targeting.CASTER, listOf(Effect.HealCaster(15), Effect.HealAdjacentAllies(15))),
            AbilityInfo("teleport", manaCost = 5, cooldown = 1, Targeting.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT, listOf(Effect.Teleport)),
            AbilityInfo("cleave", manaCost = 5, cooldown = 1, Targeting.ALL_ADJACENT_ENEMIES, listOf(Effect.Damage(15))),
            AbilityInfo("venom-strike", manaCost = 5, cooldown = 1, Targeting.ADJACENT_ENEMY, listOf(Effect.Damage(5), Effect.DamageOnPlayerTurnStarted(damage = 3, turns = 5))),
            AbilityInfo("poisoned-sword", manaCost = 5, cooldown = 1, Targeting.ADJACENT_ENEMY, listOf(Effect.DamageOnPlayerTurnStarted(damage = 3, turns = 5))),
            AbilityInfo("mushroom", manaCost = 10, cooldown = 0, Targeting.ALL_ADJACENT_ENEMIES, listOf(Effect.DamageOnPlayerTurnStarted(damage = 3, turns = 5))),
            AbilityInfo("heal", manaCost = 10, cooldown = 2, Targeting.CASTER, listOf(Effect.HealCaster(20))),
            AbilityInfo("bee", manaCost = 10, cooldown = 0, Targeting.VACANT_TILE_ADJACENT_TO_CASTER, listOf(Effect.Summon(unitId = "bee", turns = 5, baseMaximumHealth = 10))),
        ).associateBy(AbilityInfo::abilityId)

    private val sword = StoryDto(id = "screen-ability-info--sword", name = "Sword", description = "One effect, free and with no cooldown: no cooldown pill.", args = emptyMap())
    private val skull =
        StoryDto(
            id = "screen-ability-info--skull",
            name = "Skull",
            description = "An effect applied when the target is defeated, expanded one level.",
            args = mapOf(ABILITY to ValueDto.text("skull")),
        )
    private val bat = StoryDto(id = "screen-ability-info--bat", name = "Bat", description = "A summon, with its turns and health.", args = mapOf(ABILITY to ValueDto.text("bat")))
    private val mending =
        StoryDto(
            id = "screen-ability-info--mending",
            name = "Mending",
            description = "A heal of the caster and of its adjacent allies, marked green in the diagram.",
            args = mapOf(ABILITY to ValueDto.text("mending")),
        )
    private val inCooldown =
        StoryDto(
            id = "screen-ability-info--in-cooldown",
            name = "In cooldown",
            description = "Mending, cast last turn: the turns left, in an orange banner under its header.",
            args = mapOf(ABILITY to ValueDto.text("mending"), COOLDOWN_TURNS_LEFT to ValueDto.int(2)),
        )
    private val notEnoughMana =
        StoryDto(
            id = "screen-ability-info--not-enough-mana",
            name = "Not enough mana",
            description = "Bat, with 4 mana left: the mana missing, in a blue banner under its header.",
            args = mapOf(ABILITY to ValueDto.text("bat"), REMAINING_MANA to ValueDto.int(4)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-ability-info",
            group = "Screen",
            title = "Ability info",
            description = "What the selected ability does, as a card under the ability buttons: its costs as pills, why it can not be cast as a banner, its effects with their numbers in gold.",
            controls =
                listOf(
                    ControlDto.pick(ABILITY, abilities.keys.toList()),
                    ControlDto.int(COOLDOWN_TURNS_LEFT, minimum = 0, maximum = COOLDOWN_LIMIT),
                    ControlDto.int(REMAINING_MANA, minimum = 0, maximum = MANA_LIMIT),
                ),
            args = mapOf(ABILITY to ValueDto.text("sword"), COOLDOWN_TURNS_LEFT to ValueDto.int(0), REMAINING_MANA to ValueDto.int(MANA_LIMIT)),
            stories = listOf(sword, skull, bat, mending, inCooldown, notEnoughMana),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        AbilityInfoView(AbilityInfoView.WIDTH, context.resourcesLoader, context.messages).apply {
            display(abilityInfo(args))
        }

    private fun abilityInfo(args: Args): AbilityInfo = abilityInfo(args.text(ABILITY), remainingCooldown = args.int(COOLDOWN_TURNS_LEFT), remainingMana = args.int(REMAINING_MANA))

    internal fun abilityInfo(
        abilityId: String,
        remainingCooldown: Int,
        remainingMana: Int,
    ): AbilityInfo {
        val ability = abilities.getValue(abilityId)
        val reason =
            when {
                remainingCooldown > 0 -> Reason.Cooldown(remainingCooldown)
                remainingMana < ability.manaCost -> Reason.NotEnoughMana(manaCost = ability.manaCost, remainingMana = remainingMana)
                else -> null
            }
        return ability.copy(reason = reason)
    }
}
