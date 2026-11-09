# Each subdomain saves its own snapshot

Saving and loading live in the `save` subdomain, which only depends on `shared` and is only used by `screen`
(`SaveDependencyArchitectureTest`), while each subdomain holding runtime state takes and loads its own snapshot through
the `Snapshottable` port of `shared.domain`. Its `adapters.save` package writes its own JSON from `@Serializable`
classes, never annotating the domain, through a `Search…Snapshot` query and a `Load…Snapshot` command. We chose this so
`save` never knows the shape of a snapshot and the state of a subdomain only leaves it through its own use cases;
`SnapshotCoverageArchitectureTest` fails when a subdomain with a repository neither saves nor says why not. The save is
JSON in KorGE's `views.storage` (a file on the JVM, the `localStorage` of the browser), so it works on every target with
no platform-specific code.

## Consequences

- Clearing the site data of the browser deletes the saves, and there is no way to back them up.
