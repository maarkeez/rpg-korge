# Plan: product events become telemetry events, and the architecture tests check every aggregate root's events and every repository

> **Status:** done.
>
> **Summary:** Today `Telemetry` is the only aggregate root without `pullEvents()`: its `pullProductEvents()` returns
> `Telemetry.Dto.ProductEventDto`, a Dto of state it never stores, and `RecordProductEvent` hands each one to
> `TelemetryRepository.create`, the only repository method in the game taking something other than its aggregate root.
> E.g.: completing Bats in the Meadow with consent pulls a `ProductEventDto` named `QUEST_COMPLETED` and calls
> `create` with it, then `save` with the telemetry. Nothing stopped this: no architecture test says what an aggregate
> root pulls or what a repository takes, and `SaveRepository.searchSummary`, returning a `Save.Dto.SummaryDto`, slipped
> through the same gap. Instead, `Telemetry.pullEvents()` returns `TelemetryEvent.ProductEventRecorded`, the repository
> keeps only `search` and `save`, and its `save` queues the product events on the device for a sender to send to
> `RemoteTelemetryRepository` in the background, as product events will go over the network. `DomainArchitectureTest`
> then checks that every aggregate root pulls its own subdomain's events, and that every repository takes and gives
> only its aggregate root. It builds on the [telemetry plan](telemetry.md), on
> [telemetry listening and recording only with consent](../adr/0017-telemetry-listens-and-records-only-with-consent.md),
> amended for this plan, and on [subdomains as layered packages](../adr/0001-layered-subdomain-packages.md).

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                                                                                                                                                            |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope           | The telemetry product events as telemetry events, the product events queued on the device and sent in the background, an architecture test of `pullEvents()` for the 23 aggregate roots, an architecture test of the repository interfaces, and `SaveRepository.searchSummary` dropped so the save subdomain passes it.                                                                                             |
| Product event   | One `TelemetryEvent.ProductEventRecorded` carrying the name, the install id, the session id, the game id, when it happened and the quest id, slot, lootable item id, price and unit id, with the `Name` enum nested in it. Chosen over one event per product event, 13 classes each mapped again by the remote.                                                                                                     |
| Who pulls       | `FacadeTelemetryRepository.save` pulls the events of the telemetry it saves, the only repository in the game to do so. Chosen over the use case publishing them on the event bus for a subscribed remote adapter, as a product event is a fact to keep, not one another subdomain reacts to: the events it comes from are already on the bus.                                                                       |
| Network         | `save` queues the product events on the device, synchronously with the consent, and a `ProductEventSender` sends the queue in the background. Chosen over `save` sending them, which makes a command on the game loop wait on the network, and over sending fire-and-forget, which loses an event when the network fails or the game closes.                                                                        |
| Sending         | After each `save` that leaves product events queued, the sender sends the whole queue as one batch, one batch in flight at a time, and sends again if more were queued meanwhile. `StartSession` saves at every launch, so what a launch could not send is sent at the next. Chosen over a timer, one more thing to own and test, and over sending at launch only.                                                  |
| Remote          | `RemoteTelemetryRepository.create(productEvents: List<ProductEventRecorded>)` is `suspend` and throws when the remote does not accept them; the sender removes the batch from the queue only once it returns. It stays empty until the storage is chosen, accepting every batch, so nothing is kept on the device. Chosen over one call per product event.                                                          |
| Privacy         | `save` deletes the queue whenever the consent is not given on the current `PRIVACY_NOTICE_VERSION`: declining deletes the product events still waiting, and so does a new notice, at the next launch, so each is only sent under the consent it was recorded with.                                                                                                                                                  |
| Events rule     | Every aggregate root declares `fun pullEvents(): Pair<Set<<Subdomain>Event>, <Subdomain>>`, its return type written out, and no other public function starting with `pull`. Chosen over checking the type of its `events` property, which says what it pulls only indirectly.                                                                                                                                       |
| Repository rule | Every function of a repository interface in a `domain` package takes only its aggregate root or `String`, `Int` and other primitives, and returns its aggregate root, nullable or a `List` of it, or nothing. Only the interfaces: the storage classes behind a facade, such as `RemoteTelemetryRepository`, are free. Chosen over allowing only some function names, which lets `create(ProductEventDto)` through. |
| Save slots      | `SearchSaveSlots` reads each of the 3 saves whole with `search(slot)`, and `Save.slotOf(slot, save)` takes the `Save`. `save.slot<N>.summary` is no longer written, and `delete` still removes a leftover one. Chosen over allowing an aggregate root's own Dto as a return type, which loosens the rule, and over a `Save` read from its summary only, which could be saved over the whole one.                    |
| Architecture    | `telemetry.domain` (`Telemetry`, `TelemetryEvent`, `TelemetryRepository`), `telemetry.usecases.services` (`RecordProductEvent`), `telemetry.adapters.storage` (`FacadeTelemetryRepository`, `NativeStorageTelemetryRepository`, `RemoteTelemetryRepository`, `ProductEventSender`), `save.domain`, `save.usecases.queries` and `save.adapters.storage`.                                                             |
| Tests           | Unit tests of `Telemetry`, `RecordProductEvent`, the telemetry commands, the storage classes, the sender and the save, `TelemetryAcceptanceTest`, and two new tests in `DomainArchitectureTest`. No end-to-end test.                                                                                                                                                                                                |
| Out of scope    | Choosing the remote storage, a cap on the queue, waiting between retries, the privacy notice saying declining deletes the waiting product events (written when the storage is chosen, with the version bump of ADR 0017), and making every repository use `save` over `create` and `update`.                                                                                                                        |
| Delivery        | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                                                                      |

For example, a player who gave consent completes Bats in the Meadow with no network once the storage is chosen, closes
the game, and launches it again:

| Step                       | `telemetry.productEvents` on the device | Sent to `RemoteTelemetryRepository.create` |
|----------------------------|-----------------------------------------|--------------------------------------------|
| Bats in the Meadow is done | Quest completed                         | Quest completed, which throws              |
| The game is closed         | Quest completed                         | Nothing                                    |
| The game is launched again | Quest completed, then emptied           | Quest completed, accepted                  |
| Main menu                  | Main menu opened, then emptied          | Main menu opened, accepted                 |

Had the player declined in between, `save` would have deleted the queued Quest completed before the launch sent it.

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone               | Done when                                                                                                                                                              | Status | Done in |
|----|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Product events recorded | `Telemetry.pullEvents()` returns `ProductEventRecorded`, `TelemetryRepository` has only `search` and `save`, and product events are queued and sent in the background. | ✅      | d8bdd181|
| M2 | Events rule             | `DomainArchitectureTest` checks that the 23 aggregate roots pull their own subdomain's events, with the return type written out in each.                               | ✅      | 0054fca4|
| M3 | Repository rule         | `SaveRepository.searchSummary` is gone and `DomainArchitectureTest` checks the functions of every repository interface.                                                | ✅      | 3507ba84|

### M1 commits

1. `telemetry.domain`: `TelemetryEvent.ProductEventRecorded` with its `Name` enum, `Telemetry.pullEvents()` in place
   of `pullProductEvents()`, and `Telemetry.Dto` without `ProductEventDto` and `NameDto`, with the tests of `Telemetry`.
2. `telemetry.adapters.storage`: the queue in `NativeStorageTelemetryRepository`, `ProductEventSender`,
   `RemoteTelemetryRepository.create` taking a batch, and `FacadeTelemetryRepository.save` pulling and queueing; then
   `TelemetryRepository.create` removed, `RecordProductEvent` only saving, `TelemetryApi` taking a `CoroutineScope`
   from `startApp`, and the tests of the commands, the storage classes and `TelemetryAcceptanceTest`.

### M2 commits

1. The return type of `pullEvents()` written out in the 23 aggregate roots, then the events rule in
   `DomainArchitectureTest`.

### M3 commits

1. `save`: `SaveRepository.searchSummary` removed, `Save.slotOf` taking the `Save`, `SearchSaveSlots` reading whole
   saves and `NativeStorageSaveRepository` no longer writing the summary, with their tests.
2. The repository rule in `DomainArchitectureTest`.

## Reference

### telemetry.domain

- `TelemetryEvent.ProductEventRecorded(name: Name, installId: String, sessionId: String, gameId: String?,
  occurredAt: Double, questId: String?, slot: Int?, lootableItemId: String?, price: Int?, unitId: String?)` is a
  `TelemetryEvent`, with `enum class Name` holding the 13 names of today's `NameDto`, from `MAIN_MENU_OPENED` to
  `MEMBER_RECRUITED`.
- `Telemetry` holds `events: Set<TelemetryEvent>` in place of `productEvents: List<ProductEvent>`, and its private
  `record` adds a `ProductEventRecorded` to it, with the same consent, install id and session id checks as today. The
  private `ProductEvent` class goes.
- `Telemetry.pullEvents(): Pair<Set<TelemetryEvent>, Telemetry>` replaces `pullProductEvents()`.
- `Telemetry.Dto` loses `ProductEventDto` and `NameDto`, keeping `ConsentDto` and `ConsentStatusDto`.
- `TelemetryRepository` keeps `search()` and `save(telemetry)`, and loses `create`.

### telemetry.usecases

- `RecordProductEvent` saves `record(telemetry, now())` and pulls nothing.
- The 13 `Record…` and `StartGame` commands are unchanged.

### telemetry.adapters

- `NativeStorageTelemetryRepository` keeps the product events waiting to be sent under `telemetry.productEvents`, a
  JSON list of a `@Serializable` `ProductEventFile`, with `queue(productEvents)`, `searchQueued()`,
  `removeQueued(productEvents)` and `deleteQueued()`. `removeQueued` removes the ones it is given, not the first ones,
  so a queue deleted and filled again while a batch is in flight keeps its new product events.
- `FacadeTelemetryRepository.save(telemetry)` pulls the events, saves the pulled telemetry with
  `NativeStorageTelemetryRepository.save`, deletes the queue when the consent is not `GIVEN` on
  `Telemetry.PRIVACY_NOTICE_VERSION`, else queues the `ProductEventRecorded` events and, when the queue is not empty,
  calls `ProductEventSender.send()`. E.g.: `StartSession` at the launch after a new privacy notice saves a consent given on
  version 1, so the queue is deleted.
- `ProductEventSender(nativeStorageTelemetryRepository, remoteTelemetryRepository, scope: CoroutineScope)`:
  `send()` launches, in `scope`, the sending of the whole queue to `RemoteTelemetryRepository.create`, then removes
  that batch from the queue. While a batch is in flight, `send()` only notes that more are waiting, and the batch is
  followed by another once it is done. A batch that throws stays queued, until the next `save`.
- `RemoteTelemetryRepository.create(productEvents: List<TelemetryEvent.ProductEventRecorded>)` is
  `open suspend` and returns at once.
- `TelemetryApi(storage, remoteTelemetryRepository, scope, random, now)` builds the sender, and `startApp` passes it
  `CoroutineScope(views.coroutineContext)`. `SaveFixtureTest` and `ConsentPresenterTest` pass a test scope.

### Other subdomains

- The 22 other aggregate roots write out the return type of `pullEvents()`, e.g.:
  `fun pullEvents(): Pair<Set<QuestEvent>, Quest> = events to copy(events = emptySet())`.
- `SaveRepository` loses `searchSummary(slot)`.
- `Save.slotOf(slot: Int, save: Save?): Dto.SlotDto` reads the version and the summary of the save: `EMPTY` with no
  save, `INCOMPATIBLE` when its version is not `SAVE_VERSION`, else `SAVED` with its summary.
- `SearchSaveSlots` calls `saveRepository.search(slot)` for each of the 3 slots, a save it can not read still showing
  as `INCOMPATIBLE`.
- `NativeStorageSaveRepository.save` writes only `save.slot<N>`, and `delete` still removes `save.slot<N>.summary`, so
  a summary left by an earlier version goes when its slot is emptied. `SaveSummaryFile` stays, inside `SaveFile`.

### Tests

- `TelemetryTest`
  - `Telemetry should add a product event recorded stamped with every id when consent is given` (replaces
    `Telemetry should add a product event stamped with every id when consent is given`)
  - `Telemetry should forget the events once pulled` (replaces `Telemetry should forget the product events once
    pulled`)
  - The others pull with `pullEvents()`.
- `FakeTelemetryRepository` keeps the events of each telemetry saved in `recorded`, in place of `created`, and the
  command tests read it.
- `RecordProductEventTest`
  - `RecordProductEvent should record nothing when the telemetry was never kept`, reading `recorded`.
- `FacadeTelemetryRepositoryTest`
  - `FacadeTelemetryRepository should queue the product events recorded on the device when a telemetry is saved`
    (replaces `FacadeTelemetryRepository should send product events to the remote repository`)
  - `FacadeTelemetryRepository should delete the queued product events when consent is declined`
  - `FacadeTelemetryRepository should delete the queued product events when consent was given on another privacy notice`
  - `FacadeTelemetryRepository should keep the telemetry on the device`
- `NativeStorageTelemetryRepositoryTest`
  - `NativeStorageTelemetryRepository should keep the queued product events on the device`
  - `NativeStorageTelemetryRepository should remove only the product events it is given when some are sent`
- `ProductEventSenderTest`
  - `ProductEventSender should send the queue and empty it when the remote accepts it`
  - `ProductEventSender should keep the queue when the remote throws`
  - `ProductEventSender should send what was queued while a batch was in flight once it is done`
- `TelemetryAcceptanceTest`
  - Its tests read the batches sent to a recording `RemoteTelemetryRepository`, with a test scope.
  - `Telemetry should send at the next launch what the remote did not accept`
- `SaveTest`
  - `SlotOf should show the slot as incompatible when its save is of another version` (replaces `SlotOf should show the
    slot as incompatible when its summary is of another version`)
- `SearchSaveSlotsTest`
  - `SearchSaveSlots should show a slot as incompatible when its save can not be read` (replaces `SearchSaveSlots
    should show a slot as incompatible when its summary can not be read`)
- `NativeStorageSaveRepositoryTest` and `DeleteSaveTest` check the slot with `search` in place of `searchSummary`.
- `DomainArchitectureTest`
  - `should pull the events of its own subdomain when it is an aggregate root`
  - `should take and give only its aggregate root or primitives when it is a repository`

## Known limitations

- A batch in flight when the player declines is still sent: deleting the queue stops the next batches, not that one.
- A batch the remote does not accept is sent again only at the next `save`, at the latest the one of `StartSession` at
  the next launch, with no wait between retries.
- Until the storage is chosen, the queue never holds more than the batch in flight, so it has no cap.

## Changes while building

- M1 is one commit, not two: the domain without `ProductEventDto` does not compile until `TelemetryRepository.create`,
  `RecordProductEvent` and the storage classes change with it.
- `FacadeTelemetryRepository` takes `NativeStorageTelemetryRepository` and `ProductEventSender`, not the remote, which
  only the sender calls.
- `ProductEventSender` launches with KorGE's `launchImmediately`, as `NoRunBlockingArchitectureTest` forbids
  `kotlinx.coroutines.BuildersKt`, where `launch` lives.
- The tests share a `RecordingRemoteTelemetryRepository`, recording each batch and able to throw or hold a batch in
  flight, and a `ProductEventMother`, and run the sender in a `TestScope(UnconfinedTestDispatcher())`. `GiveConsentTest`
  builds the sender too.
- The events rule also allows `Pair<Set<DomainEvent>, <Subdomain>>`: `Inventory`, `BattleUnit`, `Npc`,
  `CpuWanderBrain` and `Building` also emit the request events of the subdomain asked to act, such as
  `BattlefieldEvent.RequestOccupyTile`, so they can not pull only their own. Chosen with the user over moving those
  request events, a redesign beyond this plan.
- The events rule checks that there are 23 aggregate roots, and runs only with `--rerun` after a change to a return
  type alone, as Konsist reads the sources, which are not inputs of `jvmTest`.
- `LoadGamePresenterTest`, `PauseEndToEndTest` and `SaveFixtureTest` no longer write or expect `save.slot<N>.summary`,
  changed in the save commit of M3, as they relied on the summary to show a slot.
