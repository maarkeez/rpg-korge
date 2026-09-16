
# Refactor Ability and Effects

> [!summary]
> Refactor the ability/effect model so that an ability's selected target is no longer implicitly the recipient of every effect.
>
> The refactor introduces:
>
> - explicit ability targeting;
> - effect target expressions;
> - concrete effect applications;
> - explicit effect sources;
> - ability execution as the bridge between selection and effects;
> - event listeners for effects triggered by battle events.
>
> The existing gameplay behaviour must remain unchanged throughout the refactor.

---

# 1. Goal

Replace the current relationship:

```text
Ability
    ↓
Target
    ↓
Effects applied to Target
```

with:

```text
Ability
    ↓
Targeting
    ↓
AbilityExecution
    ↓
EffectSpec[*]
    ↓
EffectApplication[*]
    ↓
Concrete Target
```

The key invariant becomes:

> **An ability's selected target is input to the ability execution. It is not implicitly the target of every effect.**

This must support all currently implemented ability/effect behaviour.

---

# 2. Existing project structure

The relevant source structure is:

```text
src/commonMain/kotlin/com/mkz/rpg/

├── ability/
│   ├── domain/
│   └── usecases/
│       ├── commands/
│       └── queries/
│
├── battle/
│   ├── domain/
│   └── usecases/
│
├── battlefield/
│   ├── domain/
│   └── usecases/
│
├── battleUnit/
│   ├── domain/
│   └── usecases/
│       ├── commands/
│       └── queries/
│
├── effect/
│   ├── domain/
│   └── usecases/
│       ├── commands/
│       └── queries/
│
├── player/
│   ├── domain/
│   └── usecases/
│
├── unit/
│   ├── domain/
│   └── usecases/
│
└── battlesetup/
    └── usecases/
        └── commands/
```

Important existing files:

```text
src/commonMain/kotlin/com/mkz/rpg/ability/domain/Ability.kt

src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt

src/commonMain/kotlin/com/mkz/rpg/battleUnit/domain/BattleUnit.kt

src/commonMain/kotlin/com/mkz/rpg/battleUnit/domain/BattleUnitEvent.kt

src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/CastAbility.kt

src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/ReceiveAbilityEffects.kt

src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/queries/WhereCanCast.kt

src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt
```

Before modifying anything, inspect the complete current implementations and all references to:

```text
Ability
Effect
Effect.Outcome
Effect.Application
TargetPattern
EffectTiming
ReceiveAbilityEffects
CastAbility
WhereCanCast
EventBus
BattleUnitEvent
EffectEvent
```

Do not rely only on the files listed above. Search the repository for all usages before changing public APIs.

---

# 3. Current behaviour that must be preserved

The refactor must continue supporting:

## Ability targeting

```text
SELF
ADJACENT_ENEMY
ALL_ADJACENT_ENEMIES
VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
```

## Effect outcomes

```text
DECREASE_HEALTH
INCREASE_HEALTH
NEGATE_INCREASE_HEALTH
TELEPORT
APPLY_EFFECT_ON_NEARBY_ALLIES
DEPLOY_BATTLE_UNIT
```

## Effect timing

```text
IMMEDIATELY
ON_TURN_STARTED
BEFORE_APPLYING_EFFECT
ON_DEFEATED
```

## Ability behaviour

```text
cost
cooldown
multiple effects
effect duration
```

## Battle behaviour

```text
movement
casting
teleportation
deployment
damage
healing
defeat
turns
rounds
victory
```

No gameplay feature should be removed merely because its implementation is being moved.

---

# 4. Target architecture

Implement the following conceptual model:

```text
Ability
 ├── Targeting
 └── EffectSpec[*]

EffectSpec
 ├── Effect
 └── TargetExpression

AbilityExecution
 ├── Caster
 ├── SelectedTarget
 └── BattleState

AbilityExecution
        │
        ▼
EffectApplication[*]

EffectApplication
 ├── Source
 ├── Target
 └── Effect
```

Concrete targets:

```text
EffectTarget
 ├── Unit
 └── Tile
```

Target expressions should initially support only what is required by the existing game plus the new separation:

```text
Caster
SelectedTarget
SelectedTile
CasterTile
NearbyAllies
```

Do not build a general-purpose targeting DSL yet.

---

# 5. Phase 0 — Establish a clean baseline

## Step 0.1 — Run the complete test suite

Before making changes:

```bash
./gradlew jvmTest
```

If the repository uses another standard Gradle test command, use the project's existing convention.

Record the baseline result.

## Step 0.2 — Inspect existing tests

Find tests covering:

```text
Ability
Effect
BattleUnit
Battlefield
CastAbility
ReceiveAbilityEffects
WhereCanCast
SetupBattle
```

Understand which tests currently encode the implicit assumption that:

```text
ability target == effect target
```

Those tests will need to be changed rather than blindly preserved.

## Step 0.3 — Search for all effect consumers

Search for:

```text
ReceiveAbilityEffects
effects
Effect.Outcome
Effect.Application
effectId
targetPattern
EffectTiming
```

Create a mental map of all dependencies before changing the domain types.

---

# 6. Phase 1 — Introduce explicit targets

Create the new target abstraction in the most appropriate existing domain.

Prefer keeping battle-specific concrete targets out of `Ability` where possible.

Introduce:

```kotlin
sealed interface EffectTarget {

    data class Unit(
        val id: BattleUnitId
    ) : EffectTarget

    data class Tile(
        val position: Position
    ) : EffectTarget
}
```

Adapt names to existing project types.

## Acceptance criteria

The domain can represent:

```text
Unit target
Tile target
```

without requiring a `BattleUnit` object for every target.

No gameplay behaviour should change yet.

---

# 7. Phase 2 — Introduce `TargetExpression`

Create:

```kotlin
sealed interface TargetExpression
```

Initially support:

```text
Caster
SelectedTarget
SelectedTile
CasterTile
NearbyAllies
```

Use existing naming conventions in the project.

The purpose is to describe **where an effect should be applied**, not what the player may select.

For example:

```text
Damage(10) → SelectedTarget

Damage(5) → Caster
```

## Important rule

Do not make `TargetExpression` responsible for changing battle state.

It only describes/resolves a target.

---

# 8. Phase 3 — Introduce `EffectSpec`

Create:

```kotlin
data class EffectSpec(
    val effect: Effect,
    val target: TargetExpression
)
```

If the existing repository stores effects by ID, preserve that approach where appropriate:

```kotlin
data class EffectSpec(
    val effectId: EffectId,
    val target: TargetExpression
)
```

Prefer the existing repository's identity/reference conventions instead of introducing unnecessary duplication.

The important property is:

```text
EffectSpec
    = Effect + target expression
```

---

# 9. Phase 4 — Refactor `Ability`

Modify:

```text
ability/domain/Ability.kt
```

so that an ability describes:

```text
Ability
 ├── cost
 ├── cooldown
 ├── targeting
 └── EffectSpec[*]
```

Replace the semantic meaning of:

```text
targetPattern
```

with the new `Targeting` concept.

Keep the existing targeting capabilities:

```text
SELF
ADJACENT_ENEMY
ALL_ADJACENT_ENEMIES
VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
```

Do not expand targeting capabilities in this phase.

## Preserve validation

Keep:

- valid ID;
- valid name;
- cost range;
- cooldown range;
- maximum number of effects;
- valid targeting configuration.

The refactor should not silently change domain invariants unrelated to targeting.

---

# 10. Phase 5 — Refactor `Effect`

Modify:

```text
effect/domain/Effect.kt
```

The effect should represent:

> **What happens and when it happens.**

It should no longer depend on the ability's selected target.

Preserve every currently supported outcome:

```text
DECREASE_HEALTH
INCREASE_HEALTH
NEGATE_INCREASE_HEALTH
TELEPORT
APPLY_EFFECT_ON_NEARBY_ALLIES
DEPLOY_BATTLE_UNIT
```

Preserve every timing:

```text
IMMEDIATELY
ON_TURN_STARTED
BEFORE_APPLYING_EFFECT
ON_DEFEATED
```

Do not delete existing effect capabilities while changing their target semantics.

---

# 11. Phase 6 — Introduce `EffectApplication`

Create a runtime concept:

```kotlin
data class EffectApplication(
    val source: BattleUnitId,
    val target: EffectTarget,
    val effect: Effect
)
```

If the project uses `EffectId` rather than carrying the full effect:

```kotlin
data class EffectApplication(
    val source: BattleUnitId,
    val target: EffectTarget,
    val effectId: EffectId
)
```

Choose the representation that best matches the existing repository patterns.

The semantic distinction is mandatory:

```text
Effect
    = what happens

EffectApplication
    = what happens + who/where + source
```

---

# 12. Phase 7 — Introduce `AbilityExecution`

Create an application/domain service appropriate to the existing architecture.

It should receive:

```text
caster
ability
selected target
battle state
```

Conceptually:

```kotlin
AbilityExecution(
    caster = caster,
    ability = ability,
    selectedTarget = selectedTarget
)
```

Its responsibility is to resolve:

```text
EffectSpec[*]
```

into:

```text
EffectApplication[*]
```

Example:

```text
Ability:

Blood Sacrifice

Targeting:
    Enemy

EffectSpec:
    Damage(10) → SelectedTarget
    Damage(5)  → Caster
```

Execution:

```text
EffectApplication(
    source = Mage,
    target = Orc,
    effect = Damage(10)
)

EffectApplication(
    source = Mage,
    target = Mage,
    effect = Damage(5)
)
```

---

# 13. Phase 8 — Extract target resolution from `ReceiveAbilityEffects`

Modify:

```text
battleUnit/usecases/commands/ReceiveAbilityEffects.kt
```

The current implementation mixes:

```text
target validation
target resolution
effect lookup
effect application
teleportation
deployment
```

Separate these responsibilities.

The target-resolution logic should become reusable by the ability execution flow.

The use case must no longer assume:

```text
resolved ability target
        =
effect recipient
```

---

# 14. Phase 9 — Refactor `CastAbility`

Modify:

```text
battleUnit/usecases/commands/CastAbility.kt
```

Keep its existing responsibilities:

- validate the ability;
- validate mana/cost;
- validate cooldown;
- validate cast availability;
- consume the cast;
- consume mana;
- start cooldown;
- emit the cast event.

Then make the cast flow provide:

```text
caster
ability
selected target
```

to the ability execution mechanism.

Do not move unrelated battle rules into `CastAbility`.

---

# 15. Phase 10 — Refactor `WhereCanCast`

Modify:

```text
battleUnit/usecases/queries/WhereCanCast.kt
```

Keep it responsible for answering:

> Where/what can this unit select for this ability?

It should continue supporting:

```text
SELF
ADJACENT_ENEMY
ALL_ADJACENT_ENEMIES
VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
```

It should **not** attempt to determine all effect recipients.

For example:

```text
WhereCanCast
```

answers:

```text
Which tiles/enemies can I select?
```

while:

```text
AbilityExecution
```

answers:

```text
Who will actually be affected?
```

This separation is critical.

---

# 16. Phase 11 — Refactor immediate effect application

Introduce:

```text
effect/usecases/commands/ApplyEffect.kt
```

or an equivalent command following the project's conventions.

It receives:

```text
EffectApplication
```

and applies the effect to its explicit target.

Examples:

```text
Damage → Unit
Heal → Unit
Teleport → Unit + Tile
Deploy → Tile
```

The command must not retrieve the ability's selected target.

It should only operate on the explicit `EffectApplication`.

---

# 17. Phase 12 — Preserve effect timing

The timing mechanism must continue working.

For:

```text
IMMEDIATELY
```

apply the effect immediately.

For:

```text
ON_TURN_STARTED
```

register/defer it until the relevant turn event.

For:

```text
BEFORE_APPLYING_EFFECT
```

apply the configured behaviour before the relevant effect.

For:

```text
ON_DEFEATED
```

resolve the defeated unit's relevant effects.

Do not implement a new scheduler unless the current architecture requires one.

First preserve the existing lifecycle semantics.

---

# 18. Phase 13 — Refactor nearby-allies effects

The existing:

```text
APPLY_EFFECT_ON_NEARBY_ALLIES
```

behaviour must remain supported.

Do not model it as:

```text
Effect → ability target
```

Instead resolve:

```text
NearbyAllies
```

from the appropriate source/target context.

For example:

```text
Source:
    defeated unit

TargetExpression:
    NearbyAllies

Effect:
    Heal(10)
```

becomes:

```text
Heal(10) → Ally A
Heal(10) → Ally B
Heal(10) → Ally C
```

Each concrete recipient should become an explicit `EffectApplication`.

---

# 19. Phase 14 — Refactor teleport

The existing teleport behaviour should become an explicit effect application.

Conceptually:

```text
Effect:
    Teleport

Target:
    Caster

Destination:
    SelectedTile
```

Do not make teleportation depend on the ability's implicit target.

The selected tile should be available as execution context.

The resulting effect application should contain enough information to determine:

```text
who moves
where they move
```

---

# 20. Phase 15 — Refactor deployment

Deployment is similar to teleportation.

Conceptually:

```text
Effect:
    DeployBattleUnit

Target:
    SelectedTile
```

The deployment effect should not require the selected tile to be represented as a battle unit.

Preserve the existing battlefield occupancy validation.

---

# 21. Phase 16 — Introduce domain events where behaviour crosses boundaries

Use domain events when one subdomain action causes another subdomain to react.

Keep the existing event-driven architecture.

Important events include:

```text
AbilityCasted

EffectReceived

BattleUnitDamaged
BattleUnitHealed
BattleUnitTeleported
BattleUnitMoved
BattleUnitDefeated

PlayerTurnStarted
PlayerDefeated
PlayerVictory
```

Do not create events for every internal method call.

An event should mean:

> Something meaningful happened in the domain.

---

# 22. Phase 17 — Add event listeners

Listeners should coordinate reactions without putting cross-subdomain orchestration inside entities.

Recommended listeners:

```text
OnAbilityCasted
OnBattleUnitDeployed
OnBattleUnitMoved
OnBattleUnitDefeated
OnPlayerTurnStarted
OnEffectReceived
```

Responsibilities:

### `OnAbilityCasted`

Starts/resolves the ability execution.

```text
AbilityCasted
    ↓
AbilityExecution
    ↓
EffectApplication[*]
```

### `OnEffectReceived`

Coordinates effect timing/application where appropriate.

### `OnBattleUnitDeployed`

Updates battlefield occupancy.

### `OnBattleUnitMoved`

Synchronizes battlefield position.

### `OnBattleUnitDefeated`

Triggers:

```text
ON_DEFEATED
```

effects and player-defeat logic.

### `OnPlayerTurnStarted`

Triggers:

```text
ON_TURN_STARTED
```

effects and turn-based state changes.

---

# 23. Phase 18 — Update `SetupBattle`

Modify:

```text
battlesetup/usecases/commands/SetupBattle.kt
```

Keep setup behaviour unchanged.

The setup must continue creating examples covering:

```text
damage
healing
negated healing
teleport
nearby allies
deployment
immediate effects
turn-start effects
before-applying effects
on-defeated effects
```

Update ability definitions to use:

```text
EffectSpec(
    effect = ...,
    target = ...
)
```

where necessary.

Setup should not contain new ability-resolution logic.

It should only compose the scenario.

---

# 24. Phase 19 — Add regression tests for existing behaviour

Before introducing new behaviour, make sure every existing mechanic has a test.

At minimum:

## Targeting

```text
SELF
ADJACENT_ENEMY
ALL_ADJACENT_ENEMIES
VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
```

## Effects

```text
decrease health
increase health
negate healing
teleport
nearby allies
deploy battle unit
```

## Timing

```text
immediate
turn started
before applying
on defeated
```

## Ability rules

```text
cost
cooldown
multiple effects
```

---

# 25. Phase 20 — Add tests for the new invariant

These tests are more important than the implementation details.

## Test 1 — Effect can target caster

```text
Ability target:
    Enemy

Effects:
    Damage(10) → SelectedTarget
    Damage(5)  → Caster
```

Expected:

```text
Enemy HP -= 10
Caster HP -= 5
```

---

## Test 2 — Ability can target a tile while affecting a unit

```text
Ability target:
    VacantTile

Effect:
    Teleport → Caster
```

Expected:

```text
Caster position = selected tile
```

---

## Test 3 — Tile target can produce multiple unit effects

```text
Ability target:
    Tile

Effect:
    Damage(10) → UnitsWithinRadius(selectedTile, 2)
```

Expected:

```text
every matching unit receives damage
```

---

## Test 4 — Selected target is not automatically affected

Create an ability whose selected target is only used as context:

```text
Target:
    Enemy

Effect:
    Heal(10) → Caster
```

Expected:

```text
Enemy unchanged
Caster healed
```

This test explicitly protects the new architectural invariant.

---

## Test 5 — Multiple different targets

```text
Damage(10) → SelectedTarget
Heal(5)     → Caster
```

Expected:

```text
two EffectApplications
two different recipients
```

---

# 26. Phase 21 — Add domain-event tests

Verify that important actions continue emitting events.

Examples:

```text
Cast ability
    → AbilityCasted

Apply damage
    → BattleUnitDamaged

Apply healing
    → BattleUnitHealed

Teleport
    → BattleUnitTeleported

Move
    → BattleUnitMoved

Defeat
    → BattleUnitDefeated

Start turn
    → PlayerTurnStarted
```

Also verify listeners react exactly once.

---

# 27. Phase 22 — Remove the old coupling

Only after the new model works, remove obsolete concepts/usages.

Search the repository for:

```text
targetPattern
Effect.Application
ReceiveAbilityEffects
```

and determine whether each remaining occurrence is still valid.

The final code must not contain logic equivalent to:

```kotlin
effect.applyTo(abilityTarget)
```

unless the effect explicitly resolved that target.

---

# 28. Phase 23 — Run the complete test suite

Run:

```bash
./gradlew jvmTest
```

Also run the project's existing lint/static-analysis/build tasks if configured.

The refactor is complete only when:

```text
existing tests pass
new targeting tests pass
new effect-application tests pass
event listener tests pass
```

---

# 29. Suggested implementation order

We should execute the phases in this exact order:

```text
1. Establish baseline
2. Search dependencies
3. Introduce EffectTarget
4. Introduce TargetExpression
5. Introduce EffectSpec
6. Refactor Ability
7. Refactor Effect
8. Introduce EffectApplication
9. Introduce AbilityExecution
10. Extract target resolution
11. Refactor CastAbility
12. Refactor WhereCanCast
13. Refactor immediate effect application
14. Preserve effect timings
15. Refactor nearby-allies effects
16. Refactor teleport
17. Refactor deployment
18. Preserve/add domain events
19. Add event listeners
20. Refactor SetupBattle
21. Add regression tests
22. Add new invariant tests
23. Remove old coupling
24. Run full verification
```

Do not skip ahead to ECS or a generic ability DSL during this refactor.

---

# 30. Definition of done

The refactor is complete when all of the following are true:

- [x] `Ability` no longer implies that all effects affect its selected target.
- [x] Ability targeting is separate from effect targeting.
- [x] `Effect` describes what happens, not who receives it.
- [x] `EffectSpec` associates an effect with a target expression.
- [x] `EffectApplication` contains a concrete target.
- [x] `EffectApplication` contains the source of the effect.
- [x] Units and tiles can both be effect targets.
- [x] Self effects work.
- [x] Selected-target effects work.
- [x] Area effects work.
- [x] Vacant-tile effects work.
- [x] Teleport still works.
- [x] Deployment still works.
- [x] Nearby-allies effects still work.
- [x] Immediate effects still work.
- [x] Turn-start effects still work.
- [x] Before-applying effects still work.
- [x] On-defeated effects still work.
- [x] Ability cost still works.
- [x] Ability cooldown still works.
- [x] Existing target patterns still work.
- [x] Existing domain events still work.
- [x] Cross-subdomain reactions use event listeners where appropriate.
- [x] `SetupBattle` still creates a valid playable scenario.
- [x] Existing tests pass.
- [x] New tests protect the target/effect separation.
- [x] No use case relies on the implicit rule `ability target == effect target`.

---

# 31. Example of the final model

A simple attack:

```text
Ability
    targeting = Enemy

    effects:
        Damage(10)
            → SelectedTarget
```

A self-costing attack:

```text
Ability
    targeting = Enemy

    effects:
        Damage(10)
            → SelectedTarget

        Damage(5)
            → Caster
```

An area attack:

```text
Ability
    targeting = Tile

    effects:
        Damage(30)
            → UnitsWithinRadius(SelectedTile, 2)
```

A teleport:

```text
Ability
    targeting = VacantTile

    effects:
        Teleport
            → Caster
            → SelectedTile
```

A summon:

```text
Ability
    targeting = VacantTile

    effects:
        DeployBattleUnit
            → SelectedTile
```

A defeat-triggered effect:

```text
Effect
    timing = OnDefeated

    effect:
        Heal(10)
            → NearbyAllies
```

---

# 32. Final architectural rule

When implementing a new ability, always answer these independently:

```text
1. What can the player select?
       ↓
   Targeting

2. What happens?
       ↓
   Effect

3. Who/what is affected?
       ↓
   TargetExpression

4. What are the concrete targets?
       ↓
   EffectApplication

5. What happened as a result?
       ↓
   Domain Event
```

The implementation should preserve this separation.

> **Target selection is input. Effects are behaviour. Effect applications are resolved consequences. Domain events communicate what happened.**

This is the boundary that the refactor is intended to establish.