# Plan: Rename the code of the unit subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The game data of a unit says `healthPoints`, `manaPoints`, `movementRange` and `abilities`. E.g.:
> `unit/knight.toml` has `healthPoints = 100`, `movementRange = 3` and six `abilities`. The [glossary](../../GLOSSARY.md)
> avoids "health points", "mana points" and "movement range". The knight's 100 health is the base maximum health of
> each knight battle unit, and its 3 steps are their maximum steps. Its six abilities are the starting abilities they
> know from their deployment, against the abilities they learn. Instead, a unit has `baseMaximumHealth`,
> `baseMaximumMana`, `maximumSteps` and `startingAbilities`, in its TOML files, `Unit` and `Unit.Dto`, and in the code
> that reads them. The game plays the same, and players read the same text. Builds on
> [Game data](../architecture/game-data.md) and the [battleUnit](battleUnit-renames.md) and
> [cpuBattleBrain](cpuBattleBrain-renames.md) renames, which left these words to this section.

## Decisions

| Topic              | Decision                                                                                                                                                                                                                                                                                                                     |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope              | The renames below, in the TOML files of `resources/unit/`, the code and tests of `unit`, and of `battleUnit`, `cpuBattleBrain`, `screen` and `storybook` where they read these names.                                                                                                                                        |
| Save format        | Not reached: units are game data, never saved. `SaveFixtureTest` builds units in memory only. `SAVE_VERSION` stays.                                                                                                                                                                                                          |
| Player text        | No change. Players keep "({health} HP)" in the summon line of the ability info. No translation key changes, in `messages.pot` nor `es.po`.                                                                                                                                                                                   |
| Health and mana    | A unit has a base maximum health and mana, the words the Battle unit section already gives its battle units. Chosen over `health` and `mana`, as Health is what a battle unit loses, and over Maximum health, which includes equipped effects.                                                                               |
| Maximum steps      | `movementRange` becomes `maximumSteps`, as the Battle unit section avoids Movement range.                                                                                                                                                                                                                                    |
| Starting abilities | `abilities` becomes `startingAbilities`, pairing with Learned ability and Starting gold. Chosen over keeping `abilities`, as Known ability now names both sources. The error over the 6 becomes `StartingAbilitiesAboveMaximum`, as the limit is Maximum abilities.                                                          |
| Summon health      | `AbilityInfo.Effect.Summon.healthPoints` becomes `baseMaximumHealth`, as it is the summoned unit's.                                                                                                                                                                                                                          |
| Words kept         | `startingGold`, `NegativeStartingGold`, `Unit.maximumAbilities`, `SearchMaximumAbilities`, `InitialiseUnits`, `UnitCreated`, `SearchUnitById`, the battle unit's own `AbilitiesAboveLimit`, and **Screen / Unit portrait**, **Unit name** and **Unit sprite** with their `unit` args, as they show a unit's images and name. |
| Tests              | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                                                                                                                   |
| Out of scope       | The open questions. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written.                                                                                                                                                                                                                |

## Renames

| Today                                                                                                           | Glossary term       | New name                                                                                   |
|-----------------------------------------------------------------------------------------------------------------|---------------------|--------------------------------------------------------------------------------------------|
| `healthPoints` (TOML key, `UnitDefinition`, `Unit.Dto`)                                                         | Base maximum health | `baseMaximumHealth`                                                                        |
| `manaPoints` (TOML key, `UnitDefinition`, `Unit.Dto`)                                                           | Base maximum mana   | `baseMaximumMana`                                                                          |
| `movementRange` (TOML key, `UnitDefinition`, `Unit.Dto`)                                                        | Maximum steps       | `maximumSteps`                                                                             |
| `abilities` (TOML key, `UnitDefinition`, `Unit.Dto`)                                                            | Starting ability    | `startingAbilities`                                                                        |
| The private `HealthPoints`, `ManaPoints`, `MovementRange`, `Abilities` of `Unit`                                | (as above)          | `BaseMaximumHealth`, `BaseMaximumMana`, `MaximumSteps`, `StartingAbilities`                |
| `UnitError.AbilitiesAboveLimit`, "Unit abilities are above their limit"                                         | Maximum abilities   | `StartingAbilitiesAboveMaximum`, "Unit starting abilities are above the maximum abilities" |
| `AbilityInfo.Effect.Summon.healthPoints`                                                                        | Base maximum health | `baseMaximumHealth`                                                                        |
| `UnitMother.healthPoints()`, `manaPoints()`, `abilities()`, `movementRange()` and the parameters of `unit(...)` | (as above)          | `baseMaximumHealth()`, `baseMaximumMana()`, `startingAbilities()`, `maximumSteps()`        |

## unit

### Resources

- The 10 files in `resources/unit/*.toml` say `baseMaximumHealth`, `baseMaximumMana`, `maximumSteps` and
  `startingAbilities`. E.g.: `unit/knight.toml` has `baseMaximumHealth = 100`, `baseMaximumMana = 30`,
  `maximumSteps = 3` and `startingAbilities = ["poisoned-sword", "mushroom", "skull", "teleport", "bee", "heal"]`.

### Domain

- `Unit` and `Unit.Dto` hold `baseMaximumHealth`, `baseMaximumMana`, `startingAbilities`, `maximumSteps` and
  `startingGold`. `StartingAbilities` fails with `StartingAbilitiesAboveMaximum` above `MAXIMUM_ABILITIES`, 6.

### Adapters

- `ResourcesUnitLoader.UnitDefinition` reads the new keys, and its comment stays on starting gold.

## Other callers

- `BattleUnit.deploy` and `BattleUnit.respawn` read `unit.baseMaximumHealth`, `unit.baseMaximumMana`,
  `unit.maximumSteps` and `unit.startingAbilities`.
- `WhatShouldCast.deployUtility` reads `deployedUnit.maximumSteps`.
- `AbilityInfoMapper` gives `Summon(baseMaximumHealth = unit.baseMaximumHealth)`, `AbilityInfoView` passes
  `effect.baseMaximumHealth` as `health`, and `AbilityInfoViewStories` builds the bat and bee summons with
  `baseMaximumHealth = 10`.
- The test files calling `UnitMother.unit(...)` with these parameters say the new names, e.g.: `BattleUnitTest`,
  `SimpleBattleAcceptanceTest`, `SkullAbilityAcceptanceTest`, `WhatShouldCastTest`, `AbilityInfoMapperTest`,
  `SelectionTest` and `SaveFixtureTest`. `ResourcesUnitLoaderTest` writes the new keys in its TOML.

## Docs

- [Game data](../architecture/game-data.md): the `unit/<unit id>.toml` example says the new keys, and its paragraph
  says a unit has 6 `startingAbilities` at most (`StartingAbilitiesAboveMaximum`).
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `UnitTest`
    - `Create should create a unit with up to 6 starting abilities, one per button of the ability bar` (replaces
      `Create should create a unit with up to 6 abilities, one per button of the ability bar`)
    - `Create should fail when the unit has more than 6 starting abilities` (replaces `Create should fail when the unit
      has more than 6 abilities`)

## Not in this plan

- `RequestUnitCreation`, `UnitEvent.RequestUnitCreation` and `OnRequestUnitCreation` have no caller in the game, only
  `RequestUnitCreationTest` and 7 acceptance tests, e.g.: `SimpleBattleAcceptanceTest`. Whether they are setup for the
  tests only, or dead code to delete.
- The `viking` unit (`startingGold = 100`) is used by nothing in the game data: the viking npc is recruited as
  `viking-warrior`. Yet [Inventory](../architecture/inventory.md#gold) gives the viking's 100 G as its example.

### Waiting for other sections

None: `unit` is the last section. It answers what `battleUnit`, `cpuBattleBrain`, `lootDrop`, `npc` and `storybook` left
waiting.
