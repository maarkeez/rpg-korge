# Package structure

The production code is split into subdomains. Each subdomain lives in its own package, `com.mkz.rpg.<subdomain>`, and is organised in clean architecture layers.

This structure is enforced by [`PackageStructureArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/PackageStructureArchitectureTest.kt).

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
