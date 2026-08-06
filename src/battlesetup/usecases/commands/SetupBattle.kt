package battlesetup.usecases.commands

import ability.domain.Ability
import ability.usecases.commands.RequestAbilityCreation
import battle.usecases.commands.*
import battlefield.usecases.commands.*
import effect.domain.Effect
import effect.domain.Effect.Dto.ApplicationDto
import effect.usecases.commands.RequestEffectCreation
import player.usecases.commands.*
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN

class SetupBattle(
    private val requestPlayerCreation: RequestPlayerCreation,
    private val initializeBattlefield: InitializeBattlefield,
    private val startFirstRound: StartFirstRound,
    private val requestEffectCreation: RequestEffectCreation,
    private val requestAbilityCreation: RequestAbilityCreation,
) {
    operator fun invoke(){
        val playerOneId = "player-one"
        val playerTwoId = "player-two"
        requestPlayerCreation(playerOneId, "Human", HUMAN)
        requestPlayerCreation(playerTwoId, "CPU", CPU)

        initializeBattlefield(8, 8, List(8){List(8){ "tile-id-$it" }})

        val lowPhysicalDamage = Effect.Dto(
            id = "low-physical-damage",
            type = "DECREASE_HEALTH",
            power = 10,
            probability = 100,
            modifiers = emptyList(),
            application = ApplicationDto(
                "IMMEDIATELY",
                onTurnStarted = null,
                beforeApplyingEffect = null
            )
        )
        requestEffectCreation(lowPhysicalDamage)

        val punch = Ability.Dto(
            id = "punch",
            name = "Punch",
            cost = 0,
            cooldown = 0,
            effects = listOf(lowPhysicalDamage.id),
            targetPattern = "ADJACENT_ENEMY",
        )
        requestAbilityCreation(punch)

        startFirstRound(listOf(playerOneId, playerTwoId))
    }
}
