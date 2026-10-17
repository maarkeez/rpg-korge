# Plan: wandering CPU battle units recover health and mana

> **Status:** in progress, milestone 1 is done.
>
> **Summary:** A CPU battle unit out of a battle recovers 10% of its maximum health and mana every time it wanders:
> every second while exploring, and once per round for those out of a battle. A CPU battle unit hurt in a battle and
> left behind is back to full health about ten seconds after the party explores again.

## Decisions

| Topic        | Decision                                                                                                                                                                                |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Amount       | 10% of its maximum health and of its maximum mana, the equipment included, rounded up, so a battle unit with any maximum recovers at least 1. Never above the maximum.                 |
| When         | Every time it wanders: every second while exploring, whether it rests or steps, also outside its territory on its way home.                                                           |
| In a battle  | The CPU battle units out of the battle recover once per round, when they wander after it. The ones in the battle do not.                                                              |
| Defeated     | A defeated battle unit never recovers. It stays stored with no health, and recovering would bring it back (e.g.: the game would no longer be over once every battle unit is defeated). |
| Display      | None: the health and mana just go up, without blinking, and show in the battle unit info when it is displayed.                                                                         |
| Constant     | `WANDERING_RECOVERY_PERCENTAGE` (10), so it can be tuned.                                                                                                                               |

For example, with the units of the resources:

| Unit | Health, mana | Recovered every second |
|------|--------------|------------------------|
| Rat  | 20, 10       | 2 health, 1 mana       |
| Goat | 30, 30       | 3 health, 3 mana       |
| Bat  | 10, 0        | 1 health               |

## battleUnit

### Domain

- Add `BattleUnit.recover(percentage)` because recovering is a change of the battle unit's health and mana: it adds the
  percentage of its maximum health and mana, rounded up and capped at the maximum, as `replenishMana` caps the mana.
  It changes nothing once defeated. It publishes no event, as nothing reacts to it.
- Add `BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds, percentage)` because the wandering requests it, as
  it requests each step with `RequestWalkBattleUnit`. The request belongs to `battleUnit`, which handles it.

### Use cases

#### Commands

- Add `RecoverBattleUnits(battleUnitIds, percentage)`: on `RequestRecoverBattleUnits`, each battle unit recovers.

### Adapters

#### Events

- Add `OnRequestRecoverBattleUnits`, wired in `BattleUnitApi`.

## wandering

### Domain

- Nothing changes: the battle units that wander are already decided when the wandering starts.

### Use cases

#### Commands

- Modify `WanderWhileExploring` so it requests the recovery of the CPU battle units that wander, before they rest or
  step. Nothing is requested when no CPU battle unit has a home.
- Modify `WanderAfterRound` so it does too, for the CPU battle units out of the battle (milestone 2). Nothing is
  requested when none is out of the battle.

#### Services

- Add `WANDERING_RECOVERY_PERCENTAGE` (10), in `WanderingRecovery.kt`, as the wandering decides how much its battle
  units recover.

### Adapters

- Nothing changes.

## screen

- Nothing changes: the recovery is not displayed. Only the battle units in a battle or of the party can be selected,
  so the info of a wandering CPU battle unit is not on screen while it recovers.

## Impact

- The acceptance tests that wire the `WanderingApi` and hurt a CPU battle unit out of the battle (e.g.: the engagement
  ones) see it recover once per round. They are checked, and updated where they assert its health.
- [CPU brain](../architecture/cpu-brain.md#wandering) explains the recovery.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                    | Changes                                                                                                                              | The game after it                                                                                          |
|---|------|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| 1 | ✅    | Recovering while exploring   | `BattleUnit.recover`, `RequestRecoverBattleUnits`, `RecoverBattleUnits`, `OnRequestRecoverBattleUnits`. `WanderWhileExploring` requests it. | The CPU battle units recover 10% of their health and mana every second while the party explores.            |
| 2 | ⬜    | Recovering when a round ends | `WanderAfterRound` requests it for the CPU battle units out of the battle.                                                           | During a battle, the CPU battle units out of it recover once per round too.                                |
