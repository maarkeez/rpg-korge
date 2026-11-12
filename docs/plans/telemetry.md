# Plan: telemetry records product events, with the player's consent

> **Status:** to do.
>
> **Summary:** Today the game knows nothing of how it is played: how many players start a new game, how many finish
> Bats in the Meadow or abandon it, how many battles end in a game over. Nothing is recorded, so every change to the
> game is a guess. Instead, a new `telemetry` subdomain listens to the domain events the other subdomains already
> publish and turns them into product events, e.g.: `QuestCompleted` for the `bats-in-the-meadow` quest becomes a
> Quest completed product event, stamped with the install id, the session id, the game id and when it happened. It
> records nothing until the player gives their consent, asked on the main menu at the first launch and changed from a
> new Privacy entry. Where product events are kept is not decided yet: `RemoteTelemetryRepository.create` does nothing,
> so this plan builds the whole path up to that one empty method. It builds on the glossary's **Product event**,
> **Install id**, **Session**, **Game id**, **Consent** and **Privacy notice**, and on
> [telemetry listening and recording only with consent](../adr/0017-telemetry-listens-and-records-only-with-consent.md),
> [subdomains as layered packages](../adr/0001-layered-subdomain-packages.md),
> [acyclic subdomain dependencies](../adr/0002-acyclic-shrink-only-subdomain-dependencies.md),
> [per-subdomain save snapshots](../adr/0010-per-subdomain-save-snapshots.md) and
> [no save migrations](../adr/0011-no-save-migrations.md).

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                                                                                                                                                                          |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The `telemetry` subdomain, its 13 product events, the install id, the session id and the game id, the consent dialog, the Privacy entry and the privacy notice panel. E.g.: a player accepts, starts a new game, buys a health potion from the merchant and saves into slot 1: Main menu opened, Game started, Item bought and Game saved reach `create`.                                                         |
| Storage        | `RemoteTelemetryRepository.create` is empty: how and where product events are kept is decided later. Everything up to it is built and tested.                                                                                                                                                                                                                                                                     |
| Placement      | A subdomain that listens to the domain events of the others, so their rules never change for telemetry. Chosen over a port in `shared.domain` each subdomain calls, which [ADR 0001](../adr/0001-layered-subdomain-packages.md) keeps to the event bus and the save, and over the screen recording them, which ties telemetry to the views.                                                                       |
| Aggregate      | `Telemetry` holds the consent, the install id, the session id and the game id, and has one method per product event, which stamps the ids on it. Chosen over a `ProductEvent` aggregate, which `DomainArchitectureTest` forbids in a `telemetry.domain` package, and over a `productEvent` package, which leaves the ids nowhere to live.                                                                         |
| Product events | One `Record…` command and one `On…` listener per product event. Chosen over one command taking a name and a map of properties, untyped, and over one listener for every event, against the one-class-per-event listeners of every other subdomain.                                                                                                                                                                |
| Request events | What no domain event says is asked for with a telemetry request event: `screen` publishes `RequestRecordMainMenuOpened`, `RequestRecordGameSaved` and `RequestRecordGameLoaded`, and `gameSetup` publishes `RequestStartGame`. Chosen over telemetry importing `screen` (a cycle) or `save` (forbidden by `SaveDependencyArchitectureTest`).                                                                      |
| Ids            | The install id is made when consent is given and kept on the device, deleted when it is withdrawn. The session id is made once per launch, in memory only. The game id is made when a new game starts, or when a save without one is loaded, and kept in the saves. Every id is 32 random hex digits from the game's `Random`.                                                                                    |
| Consent        | Nothing is recorded, nor any id written, until the player accepts, as EU law forbids storing analytics ids on the device without consent. Declining, or not answering yet, drops every product event before it reaches the repository. The choice itself is kept on the device, as it is strictly necessary to respect it.                                                                                        |
| Asking         | On the main menu at the first launch, Accept and Decline equally prominent, the game fully playable either way. Asked again when `PRIVACY_NOTICE_VERSION` changes, or 180 days after a Decline. A panel centered over the dimmed menu, blocking it until answered, chosen in M1 over a strip along its bottom, which most players would ignore, and a screen of its own, which feels like a gate before the game. |
| Withdrawing    | A Privacy entry on the main menu opens the same dialog. Declining there stops recording at once and deletes the install id. The saves keep their game id, which is no longer recorded.                                                                                                                                                                                                                            |
| Privacy notice | An in-game panel opened by Read more, localized like every other text, saying in general terms what is recorded and why. Who receives it and for how long are written once storage is chosen, bumping `PRIVACY_NOTICE_VERSION` so everyone is asked again.                                                                                                                                                        |
| Wiring         | `startApp` builds one `TelemetryApi`, which makes the session id, and hands it to every scene, which calls `telemetryApi.listen(eventBus)` on its own event bus. Chosen over handing the scenes the repository, which `RepositoryAccessArchitectureTest` forbids out of `telemetry`.                                                                                                                              |
| Saves          | `TelemetrySnapshottable` saves the game id, as the `telemetry` key, `null` without consent. `Save.SAVE_VERSION` goes from 9 to 10, so every earlier save is incompatible.                                                                                                                                                                                                                                         |
| Architecture   | `telemetry.domain`, `.usecases` and `.adapters` (`events`, `storage`, `save`, `presentation`). `telemetry` depends on `shared`, `battle`, `inventory`, `party` and `quest`, and is depended on by `screen` and `gameSetup`. The consent views and presenter live in `screen.adapters.presentation`.                                                                                                               |
| Storybook      | M1: three prototypes on a local branch, never merged. M4: **Screen / Consent › Asked**, **Screen / Privacy notice › Notice** and **Screen / Menu › Options** with its Privacy button.                                                                                                                                                                                                                             |
| Tests          | Unit tests for `Telemetry` and every command, the repositories and the snapshottable, a `TelemetryAcceptanceTest`, view and presenter tests for the consent, a `v10.json` save fixture, and the architecture tests updated. No end-to-end test.                                                                                                                                                                   |
| Out of scope   | Where product events are kept, the retention period, the processor agreement, erasing what was sent for an install id, an age check, metrics worked out from product events, and recording the consent itself as a product event.                                                                                                                                                                                 |
| Delivery       | 4 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                                                                    |

For example, a player launches the game for the first time, accepts, starts a new game, completes Bats in the Meadow,
quits to the main menu, and loads the save of slot 1, made before they ever accepted:

| Step                       | Product event reaching `create` | Install id | Session id | Game id                         |
|----------------------------|---------------------------------|------------|------------|---------------------------------|
| Main menu, dialog asked    | None, no consent yet            | None       | `s1`       | None                            |
| Accept                     | None                            | `i1`, kept | `s1`       | None                            |
| Play new game              | Game started                    | `i1`       | `s1`       | `g1`, made                      |
| Bats in the Meadow is done | Quest completed                 | `i1`       | `s1`       | `g1`                            |
| Quit to menu               | Main menu opened                | `i1`       | `s1`       | None                            |
| Load slot 1                | Game loaded                     | `i1`       | `s1`       | `g2`, made as the save had none |

## Milestones

Each milestone is a few commits straight on `main`, green on CI, except M1, which is never merged.

| #  | Milestone                | Done when                                                                                                                                         | Status | Done in |
|----|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Consent prototypes       | The three consent dialogs can be tapped through in the Storybook on `prototype/telemetry-consent`, and one is chosen.                            | ⬜      |         |
| M2 | Product events           | Every product event reaches the `Telemetry` aggregate and is dropped, as no consent can be given yet, with the architecture tests green.         | ⬜      |         |
| M3 | Game id                  | A new game makes a game id, the saves keep it, and a loaded save without one gets a new one. `SAVE_VERSION` is 10.                               | ⬜      |         |
| M4 | Consent and Privacy      | The chosen dialog asks at the first launch, Privacy changes the choice, Read more shows the notice, and with consent product events reach `create`. | ⬜      |         |

### M1 commits

1. On a local `prototype/telemetry-consent` branch, never pushed or merged: prototype stories of the three consent
   dialogs over the main menu, each with Accept, Decline and Read more, tapped through on the whole screen:
   - A: a panel centered over the main menu, like `ConfirmDialogView`, blocking the menu until answered.
   - B: a strip along the bottom of the main menu, leaving it usable; not answering counts as no consent.
   - C: a screen of its own before the main menu, shown once.
2. Screenshots of each, shared on a private Artifact page, and the chosen one written in this plan's Decisions.

### M2 commits

1. `telemetry.domain`: `Telemetry`, `TelemetryEvent` and `TelemetryRepository`, with the unit tests of `Telemetry`.
2. `telemetry.adapters.storage`: `NativeStorageTelemetryRepository`, `RemoteTelemetryRepository` and the
   `FacadeTelemetryRepository` over them, with their tests.
3. `telemetry.usecases`: `StartSession` and the 13 `Record…` and `StartGame` commands, with their tests.
4. `telemetry.adapters.events` and `TelemetryApi`, wired by `startApp`, `MenuScene`, `GameScene`, `WorldBuilderScene`,
   `PausePresenter` and `SetUpGame`, with `TelemetryAcceptanceTest` and the architecture tests updated.

### M3 commits

1. The game id in `Telemetry`, made by `StartGame` and by `RecordGameLoaded` when the loaded save has none.
2. `TelemetrySnapshottable`, in the `GameScene`'s `SaveApi`, `SAVE_VERSION` 10 and `src/jvmTest/fixtures/save/v10.json`.

### M4 commits

1. `GiveConsent`, `DeclineConsent` and `SearchConsent`, with their tests.
2. The texts of the consent, the Privacy button and the privacy notice in `messages.pot` and `es.po`.
3. The chosen consent view, `PrivacyNoticeView` and their stories.
4. `ConsentPresenter` in `MenuScene`, and the Privacy button of `MenuView`.

## Reference

### telemetry.domain

- `Telemetry` is the aggregate root, with a private constructor and `@ConsistentCopyVisibility`. Its private
  properties are the consent, the install id, the session id, the game id and the product events still to record.
- `Telemetry.notAsked()` and `Telemetry.read(dto)` are its factories. `PRIVACY_NOTICE_VERSION = 1` and
  `CONSENT_ASKED_AGAIN_AFTER_DAYS = 180` are on its companion.
- `startSession(sessionId)` sets the session id of this launch.
- One method per product event, each taking `occurredAt` and its own data, which adds a product event stamped with the
  install id, the session id and the game id when consent is given, and returns the telemetry as it is otherwise:

  | Method                                     | Product event (`NameDto`) | From                                   |
  |--------------------------------------------|---------------------------|----------------------------------------|
  | `mainMenuOpened()`                         | `MAIN_MENU_OPENED`        | `RequestRecordMainMenuOpened`          |
  | `startGame(gameId)`                        | `GAME_STARTED`            | `RequestStartGame`                     |
  | `gameLoaded(slot, newGameId)`              | `GAME_LOADED`             | `RequestRecordGameLoaded`              |
  | `gameSaved(slot)`                          | `GAME_SAVED`              | `RequestRecordGameSaved`               |
  | `questStarted(questId)`                    | `QUEST_STARTED`           | `QuestEvent.QuestStarted`              |
  | `questCompleted(questId)`                  | `QUEST_COMPLETED`         | `QuestEvent.QuestCompleted`            |
  | `questAbandoned(questId)`                  | `QUEST_ABANDONED`         | `QuestEvent.QuestAbandoned`            |
  | `battleStarted()`                          | `BATTLE_STARTED`          | `BattleEvent.BattleStarted`            |
  | `battleFinished()`                         | `BATTLE_FINISHED`         | `BattleEvent.BattleFinished`           |
  | `gameOver()`                               | `GAME_OVER`               | `PartyEvent.GameOver`                  |
  | `itemBought(lootableItemId, price)`        | `ITEM_BOUGHT`             | `InventoryEvent.ItemBought`            |
  | `itemSold(lootableItemId, sellPrice)`      | `ITEM_SOLD`               | `InventoryEvent.ItemSold`              |
  | `memberRecruited(unitId)`                  | `MEMBER_RECRUITED`        | `PartyEvent.PartyJoined`               |

- `mainMenuOpened()` forgets the game id, as no game is played on the main menu.
- `giveConsent(installId, decidedAt)` and `declineConsent(decidedAt)` change the consent, on `PRIVACY_NOTICE_VERSION`.
  Declining forgets the install id.
- `isConsentAsked(now)` is true when the consent was never asked, was given on another notice version, or was
  declined 180 days ago or more.
- `pullProductEvents()` returns the product events still to record, and the telemetry without them.
- `Telemetry.Dto` holds a `ConsentDto` (`status: ConsentStatusDto`, `NOT_ASKED`, `GIVEN` or `DECLINED`,
  `noticeVersion`, `decidedAt`), the ids, and `ProductEventDto`: `name: NameDto`, `installId`, `sessionId`, `gameId`,
  `occurredAt`, and the optional `questId`, `slot`, `lootableItemId`, `price` and `unitId`, as a choice between shapes
  is an enum with optional fields.
- `TelemetryEvent`: `RequestRecordMainMenuOpened`, `RequestStartGame`, `RequestRecordGameSaved(slot)` and
  `RequestRecordGameLoaded(slot)`.
- `TelemetryRepository`: `search(): Telemetry?`, `save(telemetry)` and `create(productEvent: Telemetry.Dto.ProductEventDto)`.

### telemetry.usecases

- `StartSession(sessionId)` takes the telemetry kept on the device, or `Telemetry.notAsked()`, and starts the session.
- Every `Record…` command, e.g. `RecordQuestCompleted(questId)`, searches the telemetry, calls its method with `now()`,
  creates every product event pulled, and saves the telemetry.
- `StartGame` does the same with a new game id. E.g.: Play new game on the main menu.
- `RecordGameLoaded(slot)` passes a new game id, used only when the loaded save had none.
- `GiveConsent` makes the install id. `DeclineConsent` declines, or withdraws a consent given.
- `SearchConsent` tells whether to ask for consent, with `isConsentAsked(now())`.
- `SearchTelemetrySnapshot` and `LoadTelemetrySnapshot` take and load the game id.

### telemetry.adapters

- `events`: `OnRequestRecordMainMenuOpened`, `OnRequestStartGame`, `OnRequestRecordGameSaved`,
  `OnRequestRecordGameLoaded`, `OnQuestStarted`, `OnQuestCompleted`, `OnQuestAbandoned`, `OnBattleStarted`,
  `OnBattleFinished`, `OnGameOver`, `OnItemBought`, `OnItemSold` and `OnPartyJoined`, each running one command.
- `storage`:
  - `FacadeTelemetryRepository` is the `TelemetryRepository`: `search` and `save` go to the native storage one,
    `create` to the remote one.
  - `NativeStorageTelemetryRepository(storage)` keeps the consent as `telemetry.consent` and the install id as
    `telemetry.installId` in `views.storage`, deleting the key when the install id is forgotten, and keeps the session
    id and the game id in memory.
  - `RemoteTelemetryRepository.create` does nothing.
- `save`: `TelemetrySnapshottable`, under the `telemetry` key, `{"gameId": "…"}`, or `{"gameId": null}` without consent.
- `presentation`: `TelemetryApi(storage, random, now)` makes the session id and runs `StartSession`. Its
  `listen(eventBus)` builds the `On…` listeners on a scene's event bus. It exposes `giveConsent`, `declineConsent`,
  `searchConsent` and `snapshottable`.

### Other subdomains

- `startApp` builds the `TelemetryApi` with `storage ?: views.storage`, `random` and `DateTime.nowUnixMillis()`, and
  `MenuScene`, `GameScene` and `WorldBuilderScene` take it and hand it on as they change to each other.
- `MenuScene` publishes `RequestRecordMainMenuOpened` on its event bus once its views are added.
- `PausePresenter` takes the `EventBus` and publishes `RequestRecordGameSaved(slot)` once `saveApi.saveGame` returns.
- `GameScene` publishes `RequestRecordGameLoaded(slot)` once `saveApi.loadGame(slot)` returns, and adds
  `telemetryApi.snapshottable` to its `SaveApi`.
- `SetUpGame` publishes `TelemetryEvent.RequestStartGame` after `PartyEvent.RequestStartExploration`, only for a new
  game.
- `Save.SAVE_VERSION` is 10.
- `MenuView` gains a fifth button, Privacy, under Language, and `onPrivacySelected`.
- `ConsentPresenter`, in `MenuScene`, shows the consent dialog when `searchConsent` says so, and when Privacy is
  tapped. Accept runs `giveConsent`, Decline runs `declineConsent`, and Read more shows `PrivacyNoticeView`, whose Back
  returns to the dialog.
- `UiMessage` gains `MENU_PRIVACY`, `CONSENT_TITLE`, `CONSENT_BODY`, `CONSENT_ACCEPT`, `CONSENT_DECLINE`,
  `CONSENT_READ_MORE`, `PRIVACY_NOTICE_TITLE`, `PRIVACY_NOTICE_BODY` and `PRIVACY_NOTICE_BACK`, on `ui.menu.privacy`,
  `ui.consent.*` and `ui.privacyNotice.*`, e.g. "Privacy" / "Privacidad", "Accept" / "Aceptar", "Decline" /
  "Rechazar", "Read more" / "Leer más".

### Storybook

- `MenuViewStories`: the `Options` story (`screen-menu--options`) shows the Privacy button.
- `ConsentView`'s stories, the panel chosen in M1: **Screen / Consent › Asked**, with an `onAccepted`, `onDeclined` and `onReadMore`
  action each.
- `PrivacyNoticeViewStories`: **Screen / Privacy notice › Notice**, in every language.
- `StorySmokeTest` lists the new stories.

### Docs

- [ADR 0017](../adr/0017-telemetry-listens-and-records-only-with-consent.md) names `FacadeTelemetryRepository`.
- `docs/architecture/dependencies.html` is regenerated with `telemetry`.

### Tests

- `TelemetryTest`
  - `Telemetry should add a product event stamped with every id when consent is given`
  - `Telemetry should add no product event when consent is not asked yet`
  - `Telemetry should add no product event when consent is declined`
  - `Telemetry should forget the install id when consent is withdrawn`
  - `Telemetry should keep the game id of the save when a game is loaded`
  - `Telemetry should take the new game id when the loaded save has none`
  - `Telemetry should forget the game id when the main menu is opened`
  - `Telemetry should ask for consent when the privacy notice version changed`
  - `Telemetry should ask for consent again when it was declined 180 days ago`
  - `Telemetry should not ask for consent when it was declined 179 days ago`
- `RecordQuestCompletedTest`, and one test class per `Record…` command
  - `RecordQuestCompleted should create a quest completed product event when consent is given`
  - `RecordQuestCompleted should create nothing when consent is declined`
- `StartGameTest`
  - `StartGame should make a game id and create a game started product event`
- `GiveConsentTest`, `DeclineConsentTest` and `SearchConsentTest`
  - `GiveConsent should make an install id kept on the device`
  - `DeclineConsent should delete the install id when consent was given`
  - `SearchConsent should ask for consent when the game is launched for the first time`
- `NativeStorageTelemetryRepositoryTest`
  - `NativeStorageTelemetryRepository should keep the consent and the install id on the device`
  - `NativeStorageTelemetryRepository should keep the session id and the game id in memory only`
- `FacadeTelemetryRepositoryTest`
  - `FacadeTelemetryRepository should send product events to the remote repository`
- `TelemetrySnapshottableTest`
  - `TelemetrySnapshottable should save the game id when consent is given`
  - `TelemetrySnapshottable should save no game id when consent is declined`
- `TelemetryAcceptanceTest`
  - `Telemetry should record a quest completed when Bats in the Meadow is completed with consent`
  - `Telemetry should record nothing when the player declined`
  - `Telemetry should stamp every product event of a launch with the same session id`
- `ConsentPresenterTest` and the chosen consent view's test
  - `ConsentPresenter should show the consent dialog when the game is launched for the first time`
  - `ConsentPresenter should show the consent dialog when Privacy is tapped`
  - `ConsentPresenter should show the privacy notice when Read more is tapped`
- `MenuViewTest`
  - `MenuView should run onPrivacySelected when Privacy is tapped`
- `SaveFixtureTest` loads `v10.json`.
- `SubdomainDependencyArchitectureTest` gains `"telemetry" to setOf("shared", "battle", "inventory", "party", "quest")`.
- `SnapshotCoverageArchitectureTest` finds `TelemetrySnapshottable`.

## Known limitations

- Nothing is kept: `RemoteTelemetryRepository.create` is empty until storage is chosen.
- Declines are never recorded, so the share of players who accept can't be worked out.
- Withdrawing deletes the install id on the device only: erasing what was already sent waits for the storage.
- The privacy notice says nothing yet of who receives the product events or for how long.
