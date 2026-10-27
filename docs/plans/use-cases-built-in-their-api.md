# Plan: Use cases built in their Api

> **Status:** done.
>
> **Summary:** A new architecture rule makes the presentation Api of each subdomain the only place where its use cases
> are instantiated: the commands, queries and service classes of `<subdomain>.usecases` are only built in
> `<subdomain>.adapters.presentation.<Subdomain>Api`. A use case that needs a service gets it through its constructor,
> from its Api, instead of building it. The nested `battlefieldHud` subdomain, whose use cases `BattlefieldPresenter`
> builds today, gets its own `BattlefieldHudApi`. 17 constructor calls in 9 files break the rule today.

## Decisions

| Topic         | Decision                                                                                                                                                         |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | The classes of `..usecases.commands`, `..usecases.queries` and `..usecases.services`. The service interfaces (`BattlefieldLoader`, …) are ports: their implementations are adapters, out of scope. |
| Allowed       | Anywhere in the file of the Api of the use case's own subdomain, `<subdomain>/adapters/presentation/<Subdomain>Api.kt`, e.g. `BattlefieldCreation` only in `BattlefieldApi.kt`. |
| Production    | Only the production code is checked: tests keep building the use case under test, with its fakes.                                                              |
| Services      | The Api builds each service once and injects it in the use cases that need it. Their constructors take the service instead of the dependencies it was built with. |
| battlefieldHud | A new `BattlefieldHudApi` holds its repository, `MovementService` and its 8 commands. `BattlefieldPresenter` builds one, so there is still one HUD per presenter. |
| Detection     | A constructor call is `<UseCase>(` in a file that sees the use case, by importing it or by being in its package, so a homonym (the private `DeployBattleUnit` outcome of `Effect`) does not count. |

## Violations today

| Use case                | Built in                                                                                   |
|-------------------------|--------------------------------------------------------------------------------------------|
| `AbilityCreation`       | `InitialiseAbilities`, `RequestAbilityCreation`.                                           |
| `BattleUnitDeployment`  | `ApplyEffect`, `InitialiseBattleUnits`, `DeployBattleUnit`.                                |
| `BattlefieldCreation`   | `InitialiseBattlefield`, `RequestBattlefieldCreation`, `PreviewBattlefield`.               |
| `MovementService` and the 8 commands of `battlefieldHud` | `BattlefieldPresenter`, of `screen`.                    |

## Services

- `InitialiseAbilities` and `RequestAbilityCreation` take `abilityCreation: AbilityCreation` instead of
  `abilityRepository` and `searchEffectById`. `AbilityApi` builds it.
- `ApplyEffect`, `InitialiseBattleUnits` and `DeployBattleUnit` take `battleUnitDeployment: BattleUnitDeployment` instead
  of `searchUnitById`, `searchPlayerById` and `canBattlefieldTileBeOccupied`, keeping `battleUnitRepository` where they
  use it themselves. `BattleUnitApi` builds it.
- `InitialiseBattlefield`, `RequestBattlefieldCreation` and `PreviewBattlefield` take
  `battlefieldCreation: BattlefieldCreation` instead of `battlefieldRepository` and `searchTerrainById`.
  `BattlefieldApi` builds it.

## battlefieldHud

- Add `screen.adapters.presentation.battlefieldHud.adapters.presentation.BattlefieldHudApi`, taking the `BattlefieldApi`,
  `BattleUnitApi`, `PlayerApi`, `BattleApi` and `EventBus`, and holding the `InMemoryBattlefieldHudRepository`,
  `MovementService` and the 8 commands, built as `BattlefieldPresenter` builds them today.
- `BattlefieldPresenter` builds a `BattlefieldHudApi` and calls its commands.

## Architecture test

Add `UseCaseInstantiationArchitectureTest`:

- For each production file, every constructor call of a use case it sees fails, unless the file is the Api of the use
  case's subdomain, e.g. `'BattlefieldCreation' must only be instantiated in 'BattlefieldApi', found in: [InitialiseBattlefield.kt:15]`.
- Check that it fails before the changes above, on the 17 calls listed, and only on them.

## Tests

- The tests of the 8 use cases taking a service build it and pass it in, e.g.
  `InitialiseBattlefield(loader, BattlefieldCreation(battlefieldRepository, SearchTerrainById(terrainRepository)), eventBus)`.
- `BattlefieldPresenterTest` and `ExplorationPresenterTest` stay unchanged: the presenter builds its own HUD Api.

## Documentation

- Add a "Use cases" section to `docs/architecture/package-structure.md` with the rule and its test.
