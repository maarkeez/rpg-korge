# Package structure

The production code is split into subdomains. Each subdomain lives in its own package, `com.mkz.rpg.<subdomain>`, and is organised in clean architecture layers.

This structure is enforced by [`PackageStructureArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/PackageStructureArchitectureTest.kt).

The dependencies between the subdomains are shown in the [Subdomain dependencies](dependencies.md) report.

## Allowed packages

A subdomain MUST only contain the following packages:

```
<subdomain>
├── adapters
│   └── <any name>      e.g.: events, presentation, storage, resources
├── domain
└── usecases
    ├── commands
    ├── queries
    └── services
```

- Classes, interfaces, objects and top-level functions MUST be declared in one of the leaf packages above.
- Nothing MUST be declared directly in `<subdomain>`, `<subdomain>.adapters` or `<subdomain>.usecases`.
- The leaf packages MUST NOT contain further sub-packages.

For example, given a subdomain called `tomatoes`:

| Declaration                                            | Allowed |
|--------------------------------------------------------|---------|
| `tomatoes.domain.Tomatoes`                             | ✅       |
| `tomatoes.usecases.commands.PlantTomatoes`             | ✅       |
| `tomatoes.adapters.storage.InMemoryTomatoesRepository` | ✅       |
| `tomatoes.Tomatoes`                                    | ❌       |
| `tomatoes.usecases.PlantTomatoes`                      | ❌       |
| `tomatoes.domain.model.Tomatoes`                       | ❌       |

## Exceptions

- `com.mkz.rpg.Main.kt` is the application entry point and lives in the root package.
- `com.mkz.rpg.screen.adapters.presentation.battlefieldHud` is a subdomain that only exists to support the presentation layer of the `screen` subdomain. It is nested in that presentation adapter and follows the same allowed packages as any other subdomain.

## Dependencies between subdomains

- Two subdomains MUST NOT depend on each other. This is enforced by [`SubdomainDependencyArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/SubdomainDependencyArchitectureTest.kt), which lists the two-way dependencies still to be removed. A pair MUST only ever be removed from that list, and the test fails when a listed pair no longer depends on each other.
- To have the upstream subdomain react to the downstream one without depending on it, the upstream subdomain declares a `Request…` event in its own events (e.g.: `BattleEvent.RequestDefeatPlayer`), and a downstream command use case publishes it (e.g.: `battleUnit`'s `RequestBattleLeave`). The upstream subdomain listens to its own request, and the data it needs travels in the event (e.g.: the player of the battle unit joining the battle), instead of being queried from the downstream subdomain.
- An event listener (`adapters/events/On…`) holds no business logic: it only takes command use cases and the `EventBus`, and never publishes an event itself. A command use case decides and publishes. This is enforced by [`EventListenerArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/EventListenerArchitectureTest.kt).

For example, `battle` does not depend on `battleUnit`, while `battleUnit` depends on `battle` and publishes its requests:

| When                                       | `battleUnit` publishes                      | `battle`                                            |
|--------------------------------------------|---------------------------------------------|-----------------------------------------------------|
| A battle unit is deployed                  | `RequestJoinBattle(battleUnitId, playerId)` | Adds it to the battle, when there is one.           |
| A battle unit is defeated, or vanishes     | `RequestLeaveBattle(battleUnitId)`          | Removes it from the battle, when there is one.      |
| ...and all their battle units are defeated | `RequestDefeatPlayer(playerId)`             | Defeats the player, in a battle or while exploring. |
