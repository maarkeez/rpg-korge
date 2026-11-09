# Plan: Rename the code of the effect subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the effect subdomain uses words the [glossary](../../GLOSSARY.md) rejects, and one word for two
> concepts. E.g.: "application" is both when venom's damage triggers (`ApplicationDto.ON_TURN_STARTED`) and Poisoned
> Sword giving venom to a goblin (`EffectApplicationDto`), the goblin's venom "applies" again at the start of each of its
> player turns (`OnTurnStartedEffectApplied`), the health potion's heal is `INCREASE_HEALTH` while the glossary says Heal,
> and the goblin is the effect's `target` while the glossary's Effect target is the ability's rule. Reading the code and
> talking about the game take a translation. Instead, the classes, functions, TOML keys and values, and saved fields use
> the glossary's words: Timing, Apply, Trigger, Recipient, Source, Amount, Duration and the outcomes' own words. The game
> plays the same, and players read the same text. Builds on [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md) and
> [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                                    |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, data and docs of `effect`, and of `ability`, `battleUnit`, `battlefield`, `cpuBattleBrain`, `lootableItem`, `party` and `screen` where they use these names.                         |
| Save format  | Reached: the saved ongoing effect's `application`, its `ON_TURN_STARTED` value and `turnsLeft`, the saved equipped effect's `points`, and the effect id `venom-on-death` change, so `SAVE_VERSION` goes from 6 to 7.        |
| Player text  | No change: players keep reading "Poisons: 3 damage each turn for 5 turns.". No translation key changes, in `messages.pot` nor `es.po`.                                                                                      |
| Apply        | Apply is only a source giving an effect to a recipient, so `ApplyEffect`, `RequestApplyEffect` and `EffectApplicationDto` keep their names. An ongoing effect's outcome taking place is a trigger.                          |
| Effect ids   | `venom-on-death` is `venom-on-defeat`, as the glossary avoids "death". Chosen over keeping data ids out of the language, as the id is read in `skull.toml`.                                                                 |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and `SaveFixtureTest` gets the `v7.json` fixture.                                                                                |
| Out of scope | The unused `NEGATE_INCREASE_HEALTH` and `BEFORE_APPLYING_EFFECT`, the "poison" names of the ability info, and the open questions. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written. |

## Renames

| Today                                                                 | Glossary term          | New name                                                                  |
|-----------------------------------------------------------------------|------------------------|---------------------------------------------------------------------------|
| `Application`, `ApplicationDto`, `ApplicationTypeDto`                 | Timing                 | `Timing`, `TimingDto`, `TimingTypeDto`                                    |
| `application` (field of `Effect.Dto`), `[application]`                | Timing                 | `timing`, `[timing]`                                                      |
| `ON_TURN_STARTED`, `OnTurnStarted`, `OnTurnStartedDto`                | On player turn started | `ON_PLAYER_TURN_STARTED`, `OnPlayerTurnStarted`, `OnPlayerTurnStartedDto` |
| `onTurnStarted`, `toTurnStartedDto`                                   | On player turn started | `onPlayerTurnStarted`, `toPlayerTurnStartedDto`                           |
| `DECREASE_HEALTH`, `DecreaseHealth`, `DecreaseHealthDto`              | Damage                 | `DAMAGE`, `Damage`, `DamageDto`                                           |
| `INCREASE_HEALTH`, `IncreaseHealth`, `IncreaseHealthDto`              | Heal                   | `HEAL`, `Heal`, `HealDto`                                                 |
| `INCREASE_MANA`, `IncreaseMana`, `IncreaseManaDto`                    | Restore                | `RESTORE_MANA`, `RestoreMana`, `RestoreManaDto`                           |
| `DEPLOY_BATTLE_UNIT`, `DeployBattleUnit`, `DeployBattleUnitDto`       | Summon                 | `SUMMON`, `Summon`, `SummonDto`                                           |
| `APPLY_EFFECT_ON_ADJACENT_ALLIES`, `ApplyEffectOnAdjacentAllies(Dto)` | Apply, Adjacent allies | `APPLY_EFFECT_TO_ADJACENT_ALLIES`, `ApplyEffectToAdjacentAllies(Dto)`     |
| `healthPoints`, `manaPoints` (maximum outcomes)                       | Amount                 | `maximumHealth`, `maximumMana`                                            |
| `NegativePower`, `PowerAboveLimit`                                    | Amount                 | `NegativeAmount`, `AmountAboveLimit`                                      |
| `InvalidEffectApplication`, `MissingEffectApplicationDetails`         | Timing                 | `InvalidEffectTiming`, `MissingEffectTimingDetails`                       |
| `NegativeApplicationDuration`, `ApplicationDurationAboveLimit`        | Duration               | `NegativeDuration`, `DurationAboveLimit`                                  |
| `EffectTargetDto`, `target` (of `EffectApplicationDto`)               | Recipient              | `RecipientDto`, `recipient`                                               |
| `ApplicationSourceDto`                                                | Source                 | `SourceDto`                                                               |
| `venom-on-death`                                                      | On defeated            | `venom-on-defeat`                                                         |

## effect

- `Effect`, `EffectError` and `ResourcesEffectLoader` follow the table, with the factories of `EffectOutcomeDto` named
  after their outcome (`damage`, `heal`, `restoreMana`, `summon`, `applyEffectToAdjacentAllies`), the domain's
  `lastsWhileEquipped`, `isWhileEquipped` and `isImmediately` kept, and the messages of the errors saying timing, duration
  and amount. `ApplicationDefinition` is `TimingDefinition`, and reads the `timing.type` and `timing.duration` keys.
- The fields of `EffectOutcomeDto` follow their outcome: `damage: DamageDto?`, `heal: HealDto?`, `restoreMana:
  RestoreManaDto?`, `summon: SummonDto?` and `applyEffectToAdjacentAllies`. The amounts inside keep their keys (`damage`,
  `healing`, `mana`), but for `maximumHealth` and `maximumMana`.

## Resources

- `resources/effect/*.toml`: the `[application]` table is `[timing]`, the `type` values follow the table, and the
  `healthPoints` and `manaPoints` keys of `maximum-health-*` and `maximum-mana-*` are `maximumHealth` and `maximumMana`.
  E.g.: `venom-damage.toml` is `type = "DAMAGE"`, `damage = 3`, then `[timing]`, `type = "ON_PLAYER_TURN_STARTED"`,
  `duration = 5`.
- `resources/effect/venom-on-death.toml` and `.png`, and `art/effect/venom-on-death.aseprite`, are `venom-on-defeat`.
  `resources/ability/skull.toml` names it.

## ability

- `Ability`: `TargetExpression` and `TargetExpressionDto` are `EffectTarget` and `EffectTargetDto`, as the effect's
  `EffectTargetDto` is now `RecipientDto`. `ResourcesAbilityLoader`, `AbilityCreation` and `AbilityMother` follow. The
  `target` TOML key stays.

## battleUnit

- `BattleUnit`:
  - `applyImmediateEffect` is `triggerImmediateEffect`, `applyOnTurnStartedEffect` is `triggerOnPlayerTurnStartedEffect`,
    and `applyOnDefeatedEffects` is `triggerOnDefeatedEffects`, also on `OngoingEffects` and its `Effect`
    (`applyOnTurnStarted` is `triggerOnPlayerTurnStarted`, and the local `effectAfterApplication` is
    `effectAfterTrigger`).
  - `receiveOnTurnStartedEffect` and `hasOnTurnStartedEffects` say on player turn started, and their `turnsLeft` is
    `remainingDuration`.
  - The private `ApplicationStatus` is `Timing`, with `OnPlayerTurnStarted(remainingDuration)`.
  - `Bonus.MaximumHealth.healthPoints` and `Bonus.MaximumMana.manaPoints` are `amount`.
  - The local `manaIncreasedEvent` is `manaRestoredEvent`.
- `BattleUnit.Dto`: `OngoingEffectsDto.onTurnStarted` is `onPlayerTurnStarted`. `OngoingEffectSnapshotDto.application`
  and its `ApplicationDto` are `timing` and `TimingDto`, with `ON_PLAYER_TURN_STARTED`, and `turnsLeft` is
  `remainingDuration`. `EquippedEffectSnapshotDto.points` is `amount`.
- `BattleUnitError`: `NotOnTurnStarted` and `InvalidEffectApplicationStatus` are `NotOnPlayerTurnStarted` and
  `InvalidEffectTiming`, with their messages.
- `BattleUnitEvent`: `OnTurnStartedEffectApplied` is `OnPlayerTurnStartedEffectTriggered`, and `BattleUnitManaIncreased`
  is `BattleUnitManaRestored`.
- `ApplyOnTurnStartedEffects` is `TriggerOnPlayerTurnStartedEffects`, followed by `OnPlayerTurnStarted` and
  `BattleUnitApi`.
- `ApplyOnDefeatedEffectsToAdjacentAllies` is `TriggerOnDefeatedEffects`, followed by `OnBattleUnitDefeated` and
  `BattleUnitApi`.
- `OnTerrainEffectTriggered` is `OnTerrainEffectApplied`.
- `ApplyEffect`, `CastResolution`, `PreviewCastOngoingEffects` (`ONGOING_APPLICATION_TYPES` is `ONGOING_TIMINGS`) and
  `PreviewCastRemainingHealth` follow the names above.

### Save

- `BattleUnitSnapshottable`: `OngoingEffect.application` and `turnsLeft` are `timing` and `remainingDuration`, and
  `EquippedEffect.points` is `amount`, with their comments.
- `Save.SAVE_VERSION` is 7, and `src/jvmTest/fixtures/save/v7.json` is written with
  `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`, keeping the older ones.

## battlefield

- `TriggerTerrainEffects` is `ApplyTerrainEffects`, and `TerrainEffectTriggered` is `TerrainEffectApplied`, as a terrain
  applies its effect. `BattlefieldApi` and `OnRoundStarted` follow. `OnRequestTerrainEffects` stays.

## cpuBattleBrain

- `WhatShouldCast` and `SearchTerrainHealthEffect` follow the names above.

## lootableItem

- `InitialiseLootableItems`: `isAppliedWhileEquipped` is `isTimedWhileEquipped`, following `TimingTypeDto.WHILE_EQUIPPED`.

## party

- `ApplyMemberEffect` follows the names above.

## screen

- `AbilityInfoMapper`, `BattleUnitInfoMapper` and `BattlefieldPresenter` follow the names above. `AbilityInfo.Effect` and
  the translation keys stay.

## Docs

- [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md): the `effect/<effect id>.toml` section uses the new table, keys and values,
  says "amounts" as "the amount", and the ability info example says `ON_DEFEATED` `APPLY_EFFECT_TO_ADJACENT_ALLIES` of an
  `ON_PLAYER_TURN_STARTED` `DAMAGE`.
- [How to add an effect outcome](../testing/how-to-add-an-effect-outcome.md): the outcome's names, and "where effects
  trigger" for "where effects are applied" when it means the outcome taking place.
- [Presentation timeline](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/presentation-timeline.md): `OnPlayerTurnStartedEffectTriggered`, "triggers" for
  "ticks".
- [Package structure](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/package-structure.md) and [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md):
  `TerrainEffectApplied` and `ApplyTerrainEffects`.
- [CPU battle brain](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/cpu-battle-brain.md): the outcome's names.
- [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md) and [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md), where they name the
  renamed fields.
- `docs/architecture/dependencies.html` is generated again with `./gradlew dependencyReport`.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert, including `EffectTest`,
  `EffectMother`, `ResourcesEffectLoaderTest`, `EffectImagesTest`, `BattleUnitTest`, `BattleUnitMother`, `ApplyEffectTest`,
  `ApplyOnTurnStartedEffectsTest` (`TriggerOnPlayerTurnStartedEffectsTest`), `OnPlayerTurnStartedTest`,
  `TriggerTerrainEffectsTest` (`ApplyTerrainEffectsTest`), `OnRequestTerrainEffectsTest`, `BattleUnitSnapshottableTest`,
  `RestoreBattleUnitsTest`, `RequestEffectApplicationToCastTargetsTest`, `PreviewCastOngoingEffectsTest`,
  `PreviewCastRemainingHealthTest`, `WhatShouldCastTest`, `AbilityInfoMapperTest`, `BattlefieldPresenterTest`,
  `EffectsViewTest`, `EffectViewTest`, `ApplyMemberEffectTest` and the acceptance tests.
- `SaveFixtureTest` checks the new `v7.json`, and refuses `v0.json` as before.

## Not in this plan

- `NEGATE_INCREASE_HEALTH` and `BEFORE_APPLYING_EFFECT` are in `Effect`, but no effect uses them, and the battle unit can
  not trigger either: `BattleUnit.applyImmediateEffect` reaches `TODO("Not implemented")` for the first, and `ApplyEffect`
  throws `FailedToReceiveAbilityEffects` for the second. Whether they should stay is still to be decided.
- `ApplyEffect.applyToTile` only accepts a summon on a tile, and its TODO asks whether other outcomes should land on a
  tile. Still to be decided.

### Waiting for other sections

- `screen`: `AbilityInfo.Effect.DamageEachTurn`, `OnDefeatedPoisonAllies` and the local `isPoison` of `AbilityInfoMapper`
  say "poison", which players read, while the glossary has no Poison.
- `lootableItem`: Consume and Equip are still to be defined, as Source mentions them.
- `terrain`: a terrain's own `effectId`, the effect it applies.

## Changes while building

- Names the plan left out follow the table: `EffectMother`'s factories are `damageEffect`, `healEffect`, `restoreManaEffect`,
  `summonEffect`, `applyEffectToAdjacentAlliesEffect` and `whileEquippedTiming`, and their `applicationType` parameter is
  `timingType`. `CastResolution.resolveTargets` is `resolveRecipients`, the parameter of `Ability.EffectTarget.of` is
  `effectTarget`, and the local `target` of `ApplyEffect.applyToTile` is `tile`.
- `BattleUnit`'s `Timing.OnPlayerTurnStarted.apply()` is `trigger()`, as it counts down one trigger.
- The error messages say timing, duration and amount, in `EffectError` and `BattleUnitError`, e.g.: "Amount cannot be
  higher than 999".
- Test names say the same words, asserting the same, e.g.: `TriggerOnPlayerTurnStartedEffect should remove the on player
  turn started effect when it is triggered for its last player turn`, and the terrain tests say the terrain effect is
  applied.
- `applyEquippedEffect` and `DeployBattleUnitEffectAcceptanceTest` keep their names: the plan did not name the first, and
  Deploy is still the glossary's word for putting a battle unit on the battlefield.
- [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md) needed no change, as it names none of the renamed fields. `v7.json`
  only differs from `v6.json` in its version, as the fixture's knight has no ongoing nor equipped effect.
- [Presentation timeline](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/presentation-timeline.md) also says the effects "triggered at the start of a
  player turn" are seen wherever the battle unit is. The ability info line of [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md)
  says "outcome, timing and effect target".
