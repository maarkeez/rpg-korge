# Subdomains are packages with clean architecture layers

The game is split into subdomains, each a package `com.mkz.rpg.<subdomain>` holding only the leaf packages `domain`,
`usecases.commands`, `usecases.queries`, `usecases.services` and `adapters.<any name>`. Only the use cases call a
repository, and `shared.domain` only holds the plumbing of the event bus and the save (`DomainEvent`, `EventBus`,
`Subscription`, `Snapshottable`), so a game rule lives in the subdomain owning its concept. We chose this so the rules
of each subdomain stay in plain Kotlin that is unit and mutation tested, and so its boundaries are checked by
architecture tests (`PackageStructureArchitectureTest`, `RepositoryAccessArchitectureTest`,
`SharedDomainArchitectureTest`, ...) rather than by review, so neither people nor coding agents erode them as the game
grows.
