# Plan: Dto of data and enums

> **Status:** done.
>
> **Summary:** A new architecture rule keeps the `Dto` of each aggregate root, and every declaration nested in it, as data
> classes and enum classes only, with companion objects for their factories. A choice between shapes is an enum `type`
> with optional fields, as `ApplicationSourceDto` and `LootTypeDto` already do, never a sealed class or interface. Only
> `Effect.Dto.EffectTargetDto` breaks the rule today: a sealed interface with `Unit(id)` and `Tile(row, column)`.

## Decisions

| Topic          | Decision                                                                                                                                                  |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The `Dto` of every aggregate root, and every class, interface and object nested in it at any depth.                                                       |
| Allowed        | `data class`, `enum class`, and companion objects, whose methods are the factories of the Dto.                                                            |
| Not allowed    | Sealed classes and interfaces, and also plain interfaces, abstract, open or plain classes, and nested objects that are not companions.                   |
| Choices        | An enum `type` with an optional field per shape, built through companion factories, as `ApplicationSourceDto` and `LootTypeDto`.                          |
| Battle unit    | `TypeDto.BATTLE_UNIT` with `battleUnitId`, as `ApplicationSourceDto.SourceTypeDto.BATTLE_UNIT` in the same Dto, and the 81 `battleUnitId` fields of the Dtos. |
| Position       | `TypeDto.POSITION` with `position: PositionDto?`, the `row` and `column` type of `Battlefield.Dto`, `LootDrop.Dto` and `Party.Dto`. `TileDto` is left out: in `Battlefield.Dto` it is what a tile holds, not where it is. |
| Wrong shapes   | `EffectApplicationDto` is a message between use cases, never read by a `fromDto`, so nothing validates it. A target without the field of its type fails as a target of the wrong shape does today: `ApplyEffect` throws `FailedToReceiveAbilityEffects`, and the previews skip it. |

`EffectTargetDto` becomes:

```kotlin
data class EffectTargetDto(
    val type: TypeDto,
    val battleUnitId: String?,
    val position: PositionDto?,
) {
    companion object {
        fun battleUnit(battleUnitId: String) = EffectTargetDto(type = BATTLE_UNIT, battleUnitId = battleUnitId, position = null)

        fun position(row: Int, column: Int) = EffectTargetDto(type = POSITION, battleUnitId = null, position = PositionDto(row, column))
    }

    enum class TypeDto {
        BATTLE_UNIT,
        POSITION,
    }

    data class PositionDto(
        val row: Int,
        val column: Int,
    )
}
```

## effect

### Domain

- Modify `Effect.Dto.EffectTargetDto` from a sealed interface to the data class above.

## battleUnit

### Use cases

- Modify `ApplyEffect` to choose between the tile and the unit by `target.type`, and to read `target.position` and
  `target.battleUnitId`, throwing `FailedToReceiveAbilityEffects` when missing. The teleport reads
  `destination.position` when the destination is a `POSITION`.
- Modify `AbilityExecution.resolveTargets` and `destinationFor` to build the targets with `EffectTargetDto.battleUnit`
  and `EffectTargetDto.position`. Its imports of `EffectTargetDto.Unit` and `EffectTargetDto.Tile` go, so `Unit` is
  `kotlin.Unit` again in that file.
- Modify `PreviewCastOngoingEffects` and `PreviewCastRemainingHealth` to read `target.battleUnitId`, skipping a target
  without one.
- Modify `ApplyOnDefeatedEffectsToNearbyAllies` to build its targets with `EffectTargetDto.battleUnit`.

## battlefield

- Modify `TriggerTerrainEffects` to build its targets with `EffectTargetDto.battleUnit`.

## party

- Modify `ApplyMemberEffect` to build its targets with `EffectTargetDto.battleUnit`.

## Architecture test

In `DomainArchitectureTest`, in `should follow domain encapsulation rules when is the aggregate root`:

- For the `Dto` and every class nested in it, assert it is a `data class` or an `enum class`, e.g.
  `Aggregate root 'Effect.Dto.EffectTargetDto' must be a data class or an enum class`.
- Assert no interface is nested in the `Dto`, and no object other than a companion object.
- Check that the assertions fail before the changes above, only on the `Effect.Dto.EffectTargetDto` interface: its
  `Unit` and `Tile` are data classes.

## Tests

- Replace `EffectTargetDto.Unit(id = ...)` with `EffectTargetDto.battleUnit(...)`, and `EffectTargetDto.Tile(...)` with
  `EffectTargetDto.position(...)`, in the 12 test files that build targets, with no change in their expectations.
- Add `ApplyEffectTest`: a battle unit target without a battle unit id, and a position target without a position, throw
  `FailedToReceiveAbilityEffects` and publish nothing.
- Modify `OnRequestEffectApplicationToOccupantsTest` to compare the battle unit ids of the targets instead of their
  `toString()`.
