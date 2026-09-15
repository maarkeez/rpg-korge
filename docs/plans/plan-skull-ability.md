# Plan - implement skull ability
We want to implement the skull ability.
- Currently it is defined in src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt and it relates to the low physical damage effect.
- Instead we want to introduce a new effect with a new effect outcome

The idea is that the ability applies a effect to an enemy. When that enemy dies (eg, player 2 battle unit), another effect will be spread to its nearby allies (player 2 battle units)

Implement the following steps in order to introduce the new feature 
## [done] Step - Add on defeated effect application type

Add a new application type “on defeated” to the Effect class and dto defined in src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt

You will need to update the related use cases too.
For example; the use case src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/ReceiveAbilityEffects.kt will need to be modified in order to handle the new effect application type. And the BattleUnit domain class will need to be modified to introduce a new ongoing effect application status  and keep track of the ongoing “On defeated” effects 

## [done] Step - Add effect outcome: apply effect on nearby allies

Add a new effect outcome “apply effect on nearby allies” to the Effect class and dto defined in src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt 
This new effect outcome requires of an effect id. Which will be the effect to apply to the nearby allies.

## [done] Step - Modify OnBattleUnitDefeated

Add the battle unit position (row and column ) to the OnBattleUnitDefeated event.

> Note: `BattleUnit` does not store its battlefield position, so the position could not be attached to the `BattleUnitDefeated` domain event at emission. Instead, the on-defeated spread is triggered synchronously from the effect-application use cases (while the position is still resolvable), which also avoids the position-removal race with the battlefield's `removeOccupant` handler. See the note under the "cast on defeated effects" step.
## [to-do] Step - Add cast on defeated effects use cases

We need to implement a use case similar to
- src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/CastAbility.kt
The new usecase must be named `CastOnDefeatedEffects`
The new usecase must be invoke from a new listener `OnBattleUnitDefeated` that will listen to `BattleUnitDefeated` events and invoke the usecase with the the battle unit id
The  use case `CastOnDefeatedEffects` will met the following acceptance criteria: 

Given a defeated battle unit
And has an on defeated ongoing effect 
And the related effect outcome is apply effect on nearby allies
When the battle unit is defeated
Then the effect to be applied will be casted to all the nearby allies 

## [to-do] Step - Add a use case acceptance test

Add a new use case acceptance test class, similar to src/jvmTest/kotlin/com/mkz/rpg/shared/usecases/acceptance/MushroomAbilityAcceptanceTest.kt , in order to test an effect that
- has application type on defeated
- Has outcome: apply effect on nearby allies

In order to make it easier to implement, you can 
- player 1 (human) has knight and 1 mage
- player 2 (cpu) has 2 rats, one next to each other
- Player 1 mage cast an ability with the effect we want to test to rat 1
- Player 1 knight kills rat 1 (easier if it has an ability which decrease health damage is applied immediately and is higher than the rat health)
- Assert: the living rat should be affected by the mage effect


## [to-do] Step - Modify setup battle

Add a new effect named
- venom on death: this new effect will be applied “on defeated” and it will have the outcome “apply effect to nearby allies” related to the venom-damage effect already defined in the SetupBattle usecase
Modify “skull” ability
- besides of applying a low physical damage, it should apply venom on death effect 

