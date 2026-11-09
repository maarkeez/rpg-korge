# Saves are not migrated

Every save holds `Save.SAVE_VERSION`, and any change to the shape of a snapshot bumps it: a save of another version is
incompatible, and can only be deleted or overwritten. We chose no migrations because the game is pre-release and its
saves are only local, so losing them costs less than writing and testing a migration for every change to a snapshot.
`SaveFixtureTest` keeps a golden save per version under `src/jvmTest/fixtures/save`, so a snapshot can not change
without the bump being deliberate.

## Consequences

- Every bump makes every earlier save of every player incompatible.
