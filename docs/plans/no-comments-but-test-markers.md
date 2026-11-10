# Plan: no comments in `src` but the Given, When and Then markers

> **Status:** done.
>
> **Summary:** Today `src` holds about 11,150 comments. 8,320 are the bare `// Given`, `// When`, `// Then` and
> `// When / Then` markers of the tests. The other 2,829, in 799 files, are mostly KDoc and line comments that say again
> what the code says. E.g.: `/** Removes the effects an unequipped item applied to its battle unit while it was equipped. */`
> on `RemoveEquippedEffects`, or the `// Storage`, `// Commands` and `// Queries` headers of `BattlefieldApi`. They go
> stale as the code changes, and they make the code harder to read, since the reader has to check whether the comment or
> the code is right. Instead, the code explains itself and the bare markers are the only comments left. Each comment
> that holds something the code doesn't show is moved into a name, a test name or an ADR, or else deleted. A new
> architecture test fails on any other comment, and its shrink-only list of subdomains still to clean empties one
> milestone at a time, like the [shrink-only subdomain dependencies](../adr/0002-acyclic-shrink-only-subdomain-dependencies.md).

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                                                                                                                                         |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | Every Kotlin file under `src`: `commonMain`, `commonTest`, `jsMain`, `jvmMain` and `jvmTest`. E.g.: production KDoc, test helper KDoc in `BattlefieldViewExtension`, scenario comments in `CpuRespawnAcceptanceTest`, and the `*Api` section headers. Chosen over all code, so the build files, `buildSrc`, `tools`, scripts and CI files keep their comments.   |
| Allowed comments | Only a line that is exactly `// Given`, `// When`, `// Then` or `// When / Then`, and only in `commonTest` and `jvmTest`. Text after a marker (e.g.: `// Given: the battle was already disengaged…`, about 125 of them) and `// And …` go. Chosen over deleting the markers and separating the sections with blank lines, as the markers stay the one exception. |
| Each comment     | Tried in order: (1) say it in code, by renaming or extracting a named function, constant or type; (2) otherwise check that a test name already states it; (3) otherwise write an ADR if it is hard to reverse, surprising and a real trade-off; (4) otherwise delete it. Chosen over writing a missing test in step 2, or deleting every comment as it stands.   |
| Renames          | A rename from step 1 keeps serialized names, resource ids, message keys, story ids and player text unchanged. The golden saves of [no save migrations](../adr/0011-no-save-migrations.md) catch a renamed Dto property.                                                                                                                                          |
| ADRs             | Written during the sweep, each in its own commit, and listed in the final report for approval.                                                                                                                                                                                                                                                                   |
| Glossary         | A deleted comment that defines a game domain term missing from `GLOSSARY.md` is listed in the final report for the user to decide. Nothing is written to the glossary during the sweep.                                                                                                                                                                          |
| TODO comments    | The three go with no follow-up: `ApplyEffect.kt:42`, `ApplyEffect.kt:90` and `DomainArchitectureTest.kt:138`. The `TODO("Not implemented")` calls in `BattleUnit` are code, so they stay.                                                                                                                                                                        |
| Guard            | `CommentArchitectureTest` fails on any comment that is not allowed, listing each as `file:line` with its text. Chosen over a written rule alone.                                                                                                                                                                                                                 |
| Guard list       | `subdomainsWithComments` lists the subdomains still to clean, and it can only shrink: a subdomain listed with no comment left fails too. Chosen over listing the 799 files, as each milestone clears whole subdomains. A listed subdomain can still gain comments until its milestone.                                                                           |
| Architecture     | `CommentArchitectureTest` and its `CommentScanner` live in `jvmTest`, in `shared.adapters.architecture`, next to `SubdomainDependencyArchitectureTest`. Neither has a comment from the start.                                                                                                                                                                    |
| Tests            | `CommentArchitectureTest` covers the scanner and the guard. The sweep is a refactor: `./gradlew jvmTest` and `./gradlew jsTest` stay green, and no test changes beyond its comments and renames.                                                                                                                                                                 |
| Out of scope     | Comments outside `src`, the markdown docs (apart from the code examples of the testing guides), a rule in the `to-plan` and `to-implement` skills, and new tests for behaviour that only a comment described.                                                                                                                                                    |
| Delivery         | 9 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                   |

For example, three comments in production code today:

| Comment                                                                                                                       | Step | Result                                                                                                                                                                                     |
|-------------------------------------------------------------------------------------------------------------------------------|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `// The percentage of the maximum, rounded up, so any maximum gives at least 1.` on `BattleUnit.shareOf`                      | 1    | The function becomes `shareRoundedUp(maximum, percentage)`, and the comment goes.                                                                                                          |
| `/** … Nothing happens when the party can not step (e.g.: it is still stepping, talking, or battling). */` on `OpenFormation` | 2    | `OpenFormationTest` already has `should not open the formation while the party is still stepping` and `… while battling`, so the KDoc goes. The talking case has no test, and it goes too. |
| `/** Removes the effects an unequipped item applied to its battle unit while it was equipped. */` on `RemoveEquippedEffects`  | 4    | The name already says it, so it goes.                                                                                                                                                      |

## Milestones

Each milestone is a few commits straight on `main`, green on CI. A milestone that clears a subdomain removes it from
`subdomainsWithComments`.

| #  | Milestone                                     | Done when                                                                                                                                                                                    | Status | Done in  |
|----|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|----------|
| M1 | Guard                                         | `CommentArchitectureTest` passes with every subdomain listed, `Main.kt` has no comment, and the testing guides state the rule.                                                               | ✅      | 61af2e2d |
| M2 | shared                                        | `shared` has no comment and is off the list. This includes the architecture tests and the test helpers such as `ViewExtension` and `SubdomainApis`.                                          | ✅      | 6db49c70 |
| M3 | Leaf subdomains                               | `effect`, `localization`, `player`, `save`, `terrain`, `unit`, `ability`, `lootableItem`, `lootTable`, `building` and `gameSetup` have no comment and are off the list (about 160 comments). | ✅      | b9d2f4b5 |
| M4 | battle, battlefield, battleUnit, quest        | These four have no comment and are off the list (about 270 comments). `ApplyEffect`'s two TODOs are gone.                                                                                    | ✅      | 1059451b |
| M5 | party, inventory, npc, lootDrop, worldBuilder | These five have no comment and are off the list (about 300 comments).                                                                                                                        | ✅      | ffe4349e |
| M6 | CPU brains                                    | `cpuBattleBrain` and `cpuWanderBrain` have no comment and are off the list (about 200 comments).                                                                                             | ✅      | 8f5e339e |
| M7 | storybook                                     | `storybook` has no comment and is off the list (about 400 comments), `jsMain`'s `PlatformStoryLocation` included.                                                                            | ✅      | a94fd006 |
| M8 | screen views                                  | The views of `screen` and their page objects and tests have no comment (about 770 comments in 129 files). `screen` stays listed.                                                             | ✅      | 24c35f03 |
| M9 | Rest of screen                                | The rest of `screen` has no comment: presenters, mappers, `selection` and their tests (about 370 comments in 72 files). `subdomainsWithComments` is gone, and the final report is written.   | ✅      | 5339b74c |

### M1 commits

1. Remove the comments of `Main.kt`, as the guard allows none outside a subdomain.
2. Add `CommentScanner` and `CommentArchitectureTest`, with every subdomain in `subdomainsWithComments`.
3. Rewrite the comment rule of `how-to-add-a-new-test.md`, and remove the comments from the code examples of the
   testing guides.

### M2 to M9 commits

1. One commit per subdomain: `Remove the comments of the <subdomain> subdomain: …`. It covers the renames and extractions
   of step 1 and the deletions. When the subdomain is cleared, the same commit removes it from
   `subdomainsWithComments`. M8 and M9 split `screen` into two commits, one per milestone. The list leaves the code
   in M9.
2. One commit per ADR written: `Record …`.

## Reference

### CommentScanner

- `CommentScanner.comments(text)` returns each comment of a Kotlin source as its line number and text: line comments,
  block comments and KDoc. It skips string literals, raw strings (`"""…"""`) and char literals. E.g.:
  `"https://maarkeez.github.io/rpg-korge/storybook/"` holds no comment, and `/** The icon of a reward item. */` is one
  comment on its first line.
- `CommentScanner.isMarker(comment)` is true for a line comment that is exactly `// Given`, `// When`, `// Then` or
  `// When / Then` after trimming.

### CommentArchitectureTest

- Reads every `.kt` file under `File("src")`, as `SaveFixtureTest` reads its fixtures, so all five source sets are
  scanned.
- A file's subdomain is the first package segment after `com.mkz.rpg`. E.g.: `com.mkz.rpg.screen.adapters.presentation`
  is `screen`. `Main.kt`, in the root package, belongs to no subdomain, so it can never hold a comment.
- `subdomainsWithComments: Set<String>` starts with the 25 subdomains that hold a comment today, the ones in the
  Milestones table.
- A comment fails when it is not a marker, or when it is a marker in `commonMain`, `jsMain` or `jvmMain`, unless its
  subdomain is listed.
- A listed subdomain with no comment left, markers aside, fails with "Remove `<subdomain>` from subdomainsWithComments".

### Docs

- `docs/testing/how-to-add-a-new-test.md`: the rule "All the tests MUST have the following three comments" becomes
  "All the tests MUST have the markers `// Given`, `// When` and `// Then`, or `// When / Then`, alone on their line.
  They are the only comments allowed in `src`, which `CommentArchitectureTest` enforces: the code and the test names
  explain the rest." The `// Class under test`, `// Test example`, `// Imports goes here` and `// Page object of
  [FinishPlayerTurnView].` comments leave its examples, and each `// src/…` path moves to the sentence before its block.
- `docs/testing/how-to-add-a-story.md`: the `/** The stories of [ManaBarView], … */` line leaves the example.

### Tests

- `CommentArchitectureTest`
  - `CommentScanner should find line comments, block comments and KDoc with their line`
  - `CommentScanner should not find a comment when the slashes are in a string, a raw string or a char`
  - `Source files should have no comment other than the markers when their subdomain is not listed`
  - `Production files should have no Given, When or Then marker when their subdomain is not listed`
  - `Listed subdomains should still have a comment, so the list can only shrink`

### Final report

Written at the end of M9, apart from the usual report:

- The ADRs written during the sweep, for approval.
- The game domain terms that a deleted comment defined and `GLOSSARY.md` lacks, for the user to decide.
- The renames and extractions of step 1, per subdomain.

## Collected for the final report

| Milestone | Renames and extractions of step 1                                                                                                                                                                                                                                                                                                                    | ADRs                                                                                                                                                                                                       | Glossary candidates                                                                                                                                                                     |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| M2        | `FixedDoublesRandom.cpuBattleUnitsAlwaysResting()` for the `FixedDoublesRandom(0.0)` of the acceptance and end-to-end tests; `blockingBuildersMissingOnJs` in `NoRunBlockingArchitectureTest`; `whyNotSaved` in `SnapshotCoverageArchitectureTest`; `widestValueWidth` in `BarView`; the Dto rules moved into the clues of `DomainArchitectureTest`. | None                                                                                                                                                                                                       | None                                                                                                                                                                                    |
| M3        | `PriceInGold` for the price of `LootableItem`; `nowUnixMillis` for the clock of `SaveGame`.                                                                                                                                                                                                                                                          | None                                                                                                                                                                                                       | None                                                                                                                                                                                    |
| M4        | `battleUnitsOfPlayersInTurns` in `BattleMother`; `SPACES_BETWEEN_COLUMNS` and `QUOTES_AND_COMMA` in `TomlBattlefieldEncoder`; `positionsInDistanceOrder` in `SearchWalkingDistance`; `VOID_TILE` in `BattlefieldMother`; `shareRoundedUp` in `BattleUnit.recover`; `CASTS_OF_EVERY_PLAYER_TURN` in `BattleUnitSnapshottable`.                        | [0014](../adr/0014-event-bus-handles-events-in-publication-order.md), for approval                                                                                                                         | None                                                                                                                                                                                    |
| M5        | `rowsPlusColumnsBetween` for the tile distance of the walking order in `Party`; `unitIdOf` in `PartyMother`; `northEastSouthWestOffsets` in `SearchAdjacentNpc`; `eastAndSouthTerrainIds` in `WorldBuilder`.                                                                                                                                         | [0014](../adr/0014-event-bus-handles-events-in-publication-order.md), extended to `Inventory`, `Npc`, the recruits and `ApplyStandingTerrainEffects`                                                       | The ownerless loot drop: the loot drop of the items no unit dropped, e.g.: a reward that did not fit in a bag. `GLOSSARY.md` only says a reward can drop.                               |
| M6        | `nearness` for how near each option is in `WhereShouldWalk`; `northEastSouthWestPositions` in `WhereShouldWander`; `isInSightOnAxis` in `IsInSight`.                                                                                                                                                                                                 | [0012](../adr/0012-greedy-utility-ai-for-cpu-battle-units.md), extended with the wandering, for approval                                                                                                   | None                                                                                                                                                                                    |
| M7        | `SAME_ZOOM_TOLERANCE` in `Viewport`; `unsearchedComponents` for the components the search of `SidebarView` filters.                                                                                                                                                                                                                                  | None                                                                                                                                                                                                       | None                                                                                                                                                                                    |
| M8        | `createdTilesBeforeRemovals` for the copy of the created tiles in `BattlefieldView`; `isPressedSinceDisplayed` for a press on `InventoryView`.                                                                                                                                                                                                       | [0015](../adr/0015-screen-replays-events-on-a-timeline.md), for approval                                                                                                                                   | The deadzone: the middle 2x2 tiles of the viewport, inside which the leader walks without the camera trailing it. `GLOSSARY.md` only says the camera trails the leader.                 |
| M9        | None                                                                                                                                                                                                                                                                                                                                                 | [0015](../adr/0015-screen-replays-events-on-a-timeline.md), extended with what the presenters keep in timeline order, the taps waiting for the timeline and the steps waiting for the player, for approval | Time stops: the party neither walks nor starts a second while it talks to an npc, sets its formation, is paused or reads a quest. `GLOSSARY.md` only says time goes on while exploring. |

## Known limitations

- Behaviour that only a comment described, with no test, is lost when step 4 deletes it. E.g.: `OpenFormation` doing
  nothing while the party talks.
- A listed subdomain can gain new comments until its milestone, since the list holds subdomains, not files.
- `CommentScanner` reads the text, not the Kotlin syntax tree: a `//` or `/*` inside a string nested in a string
  template (e.g.: `"${"//"}"`) could be misread. None is in `src` today.
- The build files, `buildSrc`, `tools`, scripts, CI files and markdown docs keep their comments.

## Changes while building

- `CommentScanner` also reads string templates (`${…}`, nested in strings and raw strings), backtick test names and
  nested block comments. So the known limitation about a `//` inside a string nested in a template no longer holds,
  and a test name such as `` `should read // when it is a test name` `` holds no comment.
- M1 took out more comments from the code examples of the testing guides than the plan listed. These were the
  placeholders of `how-to-add-a-new-test.md` and `how-to-add-an-effect-outcome.md`, now written as ellipses, the
  `// The battle has started with the human player turn.` example comment, and the two comments of
  `how-to-take-a-game-snapshot.md`.
- M8 and M9 split `screen` by kind of file rather than by count: M8 took the views, the scenes, their page objects and
  their tests (132 files), and M9 the presenters, the mappers, the presentation models (`BattleUnitInfo`, `QuestInfo`,
  `TalkInfo`, `UiMessage`, `Messages`, `BattlefieldTerrain`, `TilePosition`), `selection`, the fakes of the views and
  their tests (71 files).
