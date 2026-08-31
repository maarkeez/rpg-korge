package battlesetup.usecases.commands

import ability.domain.Ability
import ability.usecases.commands.RequestAbilityCreation
import battle.usecases.commands.*
import battlefield.usecases.commands.*
import battleunit.usecases.commands.DeployBattleUnit
import effect.domain.Effect
import unit.domain.Unit
import effect.domain.Effect.Dto.ApplicationDto
import effect.usecases.commands.RequestEffectCreation
import player.usecases.commands.*
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import unit.usecases.commands.RequestUnitCreation

class SetupBattle(
    private val requestPlayerCreation: RequestPlayerCreation,
    private val initializeBattlefield: InitializeBattlefield,
    private val startFirstRound: StartFirstRound,
    private val requestEffectCreation: RequestEffectCreation,
    private val requestAbilityCreation: RequestAbilityCreation,
    private val requestUnitCreation: RequestUnitCreation,
    private val deployBattleUnit: DeployBattleUnit,
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
        val lowDamageHeal = Effect.Dto(
            id = "low-damage-heal",
            type = "INCREASE_HEALTH",
            power = 20,
            probability = 100,
            modifiers = emptyList(),
            application = ApplicationDto(
                "IMMEDIATELY",
                onTurnStarted = null,
                beforeApplyingEffect = null
            )
        )
        requestEffectCreation(lowPhysicalDamage)
        requestEffectCreation(lowDamageHeal)

        val sword = Ability.Dto(
            id = "sword",
            name = "Sword",
            cost = 0,
            cooldown = 0,
            effects = listOf(lowPhysicalDamage.id),
            targetPattern = "ADJACENT_ENEMY",
        )
        val heal = Ability.Dto(
            id = "heal",
            name = "Heal",
            cost = 10,
            cooldown = 2,
            effects = listOf(lowDamageHeal.id),
            targetPattern = "SELF",
        )
        requestAbilityCreation(sword)
        requestAbilityCreation(heal)

        val ratUnit = Unit.Dto(
            id = "rat",
            name = "Rat",
            healthPoints = 20,
            manaPoints = 10,
            abilities = listOf(sword.id, heal.id),
            movementRange = 3
        )
        val knight = Unit.Dto(
            id = "knight",
            name = "Knight",
            healthPoints = 20,
            manaPoints = 10,
            abilities = listOf(sword.id, heal.id),
            movementRange = 3
        )
        requestUnitCreation(ratUnit)
        requestUnitCreation(knight)

        deployBattleUnit(
            battleUnitId = "player-2-unit-1",
            unitId = ratUnit.id,
            playerId= playerTwoId,
            deployAtRow = 0,
            deployAtColumn = 0,
        )
        deployBattleUnit(
            battleUnitId = "player-2-unit-2",
            unitId = ratUnit.id,
            playerId= playerTwoId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )

        deployBattleUnit(
            battleUnitId = "player-1-unit-1",
            unitId = knight.id,
            playerId= playerOneId,
            deployAtRow = 6,
            deployAtColumn = 6,
        )
        deployBattleUnit(
            battleUnitId = "player-1-unit-2",
            unitId = knight.id,
            playerId= playerOneId,
            deployAtRow = 7,
            deployAtColumn = 7,
        )

        startFirstRound(listOf(playerOneId, playerTwoId))
    }
}
