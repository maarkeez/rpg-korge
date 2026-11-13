# Plan: the telemetry collector keeps product events on Cloudflare for 3 months

> **Status:** to do.
>
> **Summary:** Today a player who accepts the consent records product events that go nowhere: e.g.: completing Bats in
> the Meadow queues a Quest completed product event on the device, and `ProductEventSender` hands it to
> `RemoteTelemetryRepository.create`, which does nothing and accepts it, so it is gone. Nobody can learn how the game is
> played, and the privacy notice still says "Who receives it and for how long: not decided yet". Instead, a telemetry
> collector, a Cloudflare Worker in a new `telemetry-collector/` folder, receives the product events of the game deployed
> to GitHub Pages, keeps them in a D1 database in the EU, and deletes each one 3 months after it was received; the game
> sends its queued product events to it in batches of at most 100, drops a batch the telemetry collector rejects, keeps
> one it could not receive, and never queues more than 1,000; and the privacy notice says who receives them and for how
> long, asking every player again. It builds on the glossary's **Telemetry collector**, **Queued product event** and
> **Retention period**, on the [telemetry plan](telemetry.md), and on
> [telemetry listening and recording only with consent](../adr/0017-telemetry-listens-and-records-only-with-consent.md)
> and [product events kept by our own telemetry collector on Cloudflare](../adr/0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md).

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                                                                                                                                                              |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | The telemetry collector (its Worker, its D1 database, its workflow), the game sending to it, and the privacy notice naming it. E.g.: a player on GitHub Pages accepts, completes Bats in the Meadow, and one row of `QUEST_COMPLETED` lands in D1, deleted 90 days later.                                                                                                             |
| Hosting          | A Cloudflare Worker, `rpg-telemetry-collector`, on the free plan, at its `workers.dev` address, with a D1 database, `rpg-telemetry`, created in the EU jurisdiction. Chosen over PostHog, Aptabase, Supabase, Workers Analytics Engine and self-hosting, as [ADR 0018](../adr/0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md) records.                      |
| Retention period | 3 months: a scheduled run every night at 03:17 UTC deletes the rows received more than 90 days ago. Counted from when the telemetry collector received a row, as the time the game sends comes from the player's clock.                                                                                                                                                               |
| What is kept     | The fields of each product event as the game sends them and `received_at`, never the IP address, the browser or the country of the request.                                                                                                                                                                                                                                           |
| Language         | TypeScript, Cloudflare's own language for Workers. Chosen over Kotlin/JS, which needs hand-written D1 bindings and only pays off once the game is split into Gradle modules to share the product event classes.                                                                                                                                                                       |
| Contract         | A sample batch of the 13 product events, `src/jvmTest/fixtures/telemetry/product-events.json`, written by a test of the game and read by the tests of the telemetry collector, so a change to the shape on one side breaks a test.                                                                                                                                                    |
| Senders          | Only the game deployed to GitHub Pages: `deploy-js.yml` passes the telemetry collector's address as the Gradle property `rpg.telemetryCollectorUrl`, and every other build (local, JVM, tests) gets `null` and sends nothing. Chosen over checking the page's host at runtime, which hard-codes the host in the game. The Storybook never builds a `TelemetryApi`, so it never sends. |
| Abuse            | Only cheap checks: the shape and name of each product event, at most 100 per batch and 64 KB per request, the `https://maarkeez.github.io` origin only, and 60 requests a minute for each IP address, which is used as the key and never stored. Forged product events can still get in.                                                                                              |
| Answers          | 204 when a batch is kept, 400 when it is malformed, 403 for another origin, 429 over the rate limit, and 5xx when D1 fails, e.g. over the free plan's 100,000 rows written a day. The game drops a batch on a 4xx other than 429, as sending it again would fail again and block the ones after it, and keeps it on a 429, a 5xx or no answer.                                        |
| Duplicates       | A batch kept but whose answer was lost is sent again: rows are keyed by install id, session id, name and when it happened, and a row already kept is ignored.                                                                                                                                                                                                                         |
| Queue            | At most 1,000 queued product events on the device, the oldest dropped first.                                                                                                                                                                                                                                                                                                          |
| Privacy notice   | Says the product events go to the game's own server, run by Cloudflare in the EU, are deleted after 3 months, and that declining deletes the ones still waiting; gives the repository's GitHub issues as the contact, without naming its author. `PRIVACY_NOTICE_VERSION` goes from 1 to 2, so every player is asked again.                                                           |
| CI               | Its own workflow, `telemetry-collector.yml`, testing the telemetry collector and the game against it, and deploying it on `main`. Chosen over steps in `gradle.yml` and `deploy-js.yml`, which run for every change to the game.                                                                                                                                                      |
| Architecture     | The telemetry collector lives outside the game, in `telemetry-collector/`. In the game, only `telemetry.adapters.storage` changes, and `startApp`.                                                                                                                                                                                                                                    |
| Storybook        | **Screen / Privacy notice › Notice** shows the new text. No story is added.                                                                                                                                                                                                                                                                                                           |
| Tests            | Vitest on the Worker with a local D1, a fixture test of the sample batch, unit tests of `RemoteTelemetryRepository`, `ProductEventSender` and `NativeStorageTelemetryRepository`, and an end-to-end test sending from the game to the telemetry collector run by `wrangler dev`.                                                                                                      |
| Out of scope     | Charts, erasing what was received for an install id, the game version on each product event, sending from a build other than GitHub Pages, and recording the declines.                                                                                                                                                                                                                |
| Delivery         | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                                        |

For example, a player opens the game on GitHub Pages, accepts the privacy notice version 2, plays a new game and
completes Bats in the Meadow, while the telemetry collector is down for a while:

| Step                                            | Product events sent | Answer                          | Rows in D1 | Queued after |
|-------------------------------------------------|---------------------|---------------------------------|------------|--------------|
| Game started                                    | 1                   | 204                             | 1          | 0            |
| Quest started, the telemetry collector is down  | 1                   | None                            | 1          | 1            |
| Battle started                                  | 2                   | None                            | 1          | 2            |
| Battle finished, it is back, the answer is lost | 3                   | 204, never received             | 4          | 3            |
| Quest completed                                 | 4                   | 204, the 3 already kept ignored | 5          | 0            |
| 90 days after they were received                | None                | None                            | 0          | 0            |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone             | Done when                                                                                                                                                                                      | Status | Done in |
|----|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Telemetry collector   | The Worker is deployed by `telemetry-collector.yml`, keeps the sample batch in the D1 database in the EU, rejects a malformed one, and deletes rows older than 90 days.                        | ⬜      |         |
| M2 | Sending               | The game sends its queued product events in batches of 100 to a `rpg.telemetryCollectorUrl`, and the end-to-end test passes against `wrangler dev`. Nothing is deployed with the property yet. | ⬜      |         |
| M3 | Privacy notice and on | The privacy notice version 2 names the telemetry collector, its screenshots are approved, and `deploy-js.yml` passes the property, so the game on GitHub Pages sends.                          | ⬜      |         |

### M1 commits

1. The sample batch, `src/jvmTest/fixtures/telemetry/product-events.json`, and `ProductEventBatchFixtureTest`, which
   writes it from the game's own JSON with `-Drpg.updateTelemetryFixtures=true`. Its JSON moves from
   `NativeStorageTelemetryRepository` to `ProductEventJson`, used by both.
2. `telemetry-collector/`: `package.json`, `wrangler.jsonc`, `tsconfig.json`, the migration
   `migrations/0001_create_product_events.sql`, `src/index.ts`, `src/productEvents.ts`, the Vitest tests and
   `README.md`, with the steps done by hand on Cloudflare.
3. `.github/workflows/telemetry-collector.yml`: the tests on every push touching the telemetry collector, the migrations
   and the deploy on `main`, once `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` are secrets of the repository.

### M2 commits

1. `NativeStorageTelemetryRepository.queue` keeps at most `MAX_QUEUED_PRODUCT_EVENTS`, 1,000, the oldest dropped first.
2. `RemoteTelemetryRepository` sends over HTTP and answers a `Delivery`, and `ProductEventSender` sends batches of at
   most 100, dropping the rejected ones, with their tests.
3. The Gradle property `rpg.telemetryCollectorUrl`, the generated `TELEMETRY_COLLECTOR_URL`, and `startApp` building the
   `RemoteTelemetryRepository` with it.
4. `RemoteTelemetryRepositoryEndToEndTest`, the `jvmTelemetryCollectorTest` task, and the job of
   `telemetry-collector.yml` running it against `wrangler dev`.

### M3 commits

1. The privacy notice's new text in `messages.pot` and `es.po`, and `PRIVACY_NOTICE_VERSION` 2, with screenshots of
   **Screen / Privacy notice › Notice** in English and Spanish shared on a private Artifact page for approval.
2. `deploy-js.yml` passes `-Prpg.telemetryCollectorUrl=https://rpg-telemetry-collector.<subdomain>.workers.dev`.

## Reference

### telemetry-collector

- `wrangler.jsonc`: the Worker `rpg-telemetry-collector`, the D1 binding `DB` to `rpg-telemetry`, the rate limiting
  binding `RATE_LIMITER` (60 requests a 60s period), the cron trigger `17 3 * * *`, and the variable `ALLOWED_ORIGIN`,
  `https://maarkeez.github.io`. Local runs (`wrangler dev`, Vitest) set `ALLOWED_ORIGIN` to `*`, so the JVM, which sends
  no origin, can be tested.
- `migrations/0001_create_product_events.sql`: the table `product_events`, with `install_id`, `session_id`, `game_id`,
  `name`, `occurred_at`, `quest_id`, `slot`, `lootable_item_id`, `price`, `unit_id` and `received_at` (milliseconds since
  1970), its primary key `(install_id, session_id, name, occurred_at)` without a row id, and an index on `received_at`.
- `src/index.ts`:
  - `fetch` answers `POST /product-events` with `{"productEvents": [...]}`, as the game sends it, and `OPTIONS` with the
    CORS headers for `ALLOWED_ORIGIN`, `POST` and `Content-Type`, kept a day. Anything else is a 404 or a 405.
  - It checks, in this order: the origin (403), the rate limit by `CF-Connecting-IP` (429), the size, at most 64 KB, and
    the batch (400), then inserts every row in one `DB.batch` of `INSERT OR IGNORE`, stamped with the same
    `received_at`, and answers 204. A failing D1 answers 500.
  - `scheduled` runs `DELETE FROM product_events WHERE received_at < ?`, 90 days before now.
- `src/productEvents.ts`: `parseBatch(body)` returns the rows, or why it is rejected: 1 to `MAX_BATCH_SIZE`, 100,
  product events, each with exactly the fields of the sample batch, a `name` among the 13, `installId` and `sessionId`
  of 32 lowercase hex digits, `gameId` the same or `null`, `occurredAt` a number, and the optional fields `null` or
  of their type, strings of at most 64 characters.
- `README.md`: what is done by hand once, with links: create the Cloudflare account and choose its `workers.dev`
  subdomain, `wrangler d1 create rpg-telemetry --jurisdiction eu` and the database id written in `wrangler.jsonc`, an
  API token allowed to edit Workers and D1, the two secrets of the repository, and how to query the product events with
  `wrangler d1 execute rpg-telemetry --remote --command "..."` or the D1 console, e.g. how many completed
  `bats-in-the-meadow` this month.
- Node 22, Wrangler, Vitest and `@cloudflare/vitest-pool-workers`, pinned in `package.json` and `package-lock.json`.

### telemetry.adapters.storage

- `ProductEventJson`: the JSON of a product event, as `ProductEventFile` held it in `NativeStorageTelemetryRepository`,
  used by it for the queue and by `RemoteTelemetryRepository` for the requests.
- `NativeStorageTelemetryRepository.queue` keeps the last `MAX_QUEUED_PRODUCT_EVENTS`, 1,000. E.g.: with 1,000 queued,
  a Battle started product event drops the oldest one.
- `RemoteTelemetryRepository(collectorUrl: String?, httpClient: HttpClient)`: `create(productEvents)` answers a
  `Delivery`:
  - `ACCEPTED` without sending when `collectorUrl` is `null`, as today.
  - Otherwise it posts them to `$collectorUrl/product-events` as `application/json`: `ACCEPTED` on a 2xx, `REJECTED` on
    a 4xx other than 429, and `NOT_RECEIVED` on a 429, a 5xx, or a failed request.
- `ProductEventSender` sends the first `MAX_BATCH_SIZE`, 100, queued product events at a time, removes them once
  `ACCEPTED` or `REJECTED`, and stops at `NOT_RECEIVED`, trying again at the next product event or launch. E.g.: 250
  queued are sent as 100, 100 and 50.
- `TELEMETRY_COLLECTOR_URL: String?`, generated into `build/generated/telemetryCollector` from the Gradle property
  `rpg.telemetryCollectorUrl`, `null` without it.

### Other parts of the game

- `startApp` builds `RemoteTelemetryRepository(TELEMETRY_COLLECTOR_URL, createHttpClient())`.
- `build.gradle.kts`: the task generating `TELEMETRY_COLLECTOR_URL`, a source of `commonMain`; `jvmTest` leaves out the
  tests tagged `telemetry-collector`, and `jvmTelemetryCollectorTest` runs only them, with `rpg.telemetryCollectorUrl`.
- `Telemetry.PRIVACY_NOTICE_VERSION` is 2.
- `ui.privacyNotice.body`, in `messages.pot` and `es.po`, replaces its "Who receives it" and "Changing your mind"
  paragraphs and adds a "Questions" one:

  > Who receives it and for how long: the game's own server, run by Cloudflare in the European Union, which deletes it
  > 3 months after receiving it. It never keeps your IP address.
  >
  > Changing your mind: Privacy on the main menu, at any time. Declining there stops recording, deletes the id of this
  > copy of the game and what is still waiting to be sent. What was already sent is deleted after 3 months.
  >
  > Questions: open an issue at github.com/maarkeez/rpg-korge/issues.

### Workflows

- `telemetry-collector.yml` runs on a push to `main` touching `telemetry-collector/**`,
  `src/commonMain/kotlin/com/mkz/rpg/telemetry/adapters/storage/**` or `src/jvmTest/fixtures/telemetry/**`, and by hand:
  - `test`: `npm ci` and Vitest; then the D1 migrations applied locally, `wrangler dev` started on port 8787, and
    `./gradlew jvmTelemetryCollectorTest -Prpg.telemetryCollectorUrl=http://localhost:8787`, followed by a check that
    the local D1 holds the 13 product events of the sample batch once.
  - `deploy`, after `test`: `wrangler d1 migrations apply rpg-telemetry --remote` and `wrangler deploy`.
- `deploy-js.yml` passes `-Prpg.telemetryCollectorUrl` to `browserReleaseWebpack` from M3.

### Docs

- [ADR 0017](../adr/0017-telemetry-listens-and-records-only-with-consent.md): where it says the storage is left empty,
  it points to [ADR 0018](../adr/0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md).
- `telemetry-collector/README.md`, as above.

### Tests

- `telemetry-collector/test/collector.test.ts`
  - `the telemetry collector should keep every product event of the sample batch`
  - `the telemetry collector should ignore a batch it already kept`
  - `the telemetry collector should reject a product event with an unknown name`
  - `the telemetry collector should reject a batch of 101 product events`
  - `the telemetry collector should reject another origin`
  - `the telemetry collector should keep nothing but the product events and when they were received`
  - `the telemetry collector should delete the product events received more than 90 days ago`
  - `the telemetry collector should keep the product events received 89 days ago`
- `ProductEventBatchFixtureTest`
  - `The sample batch should be what the game sends for the 13 product events`
- `RemoteTelemetryRepositoryTest`
  - `RemoteTelemetryRepository should accept without sending when there is no telemetry collector`
  - `RemoteTelemetryRepository should be accepted when the telemetry collector answers 204`
  - `RemoteTelemetryRepository should be rejected when the telemetry collector answers 400`
  - `RemoteTelemetryRepository should not be received when the telemetry collector answers 429`
  - `RemoteTelemetryRepository should not be received when the telemetry collector answers 500`
  - `RemoteTelemetryRepository should not be received when the request fails`
- `ProductEventSenderTest`
  - `ProductEventSender should send batches of 100 product events`
  - `ProductEventSender should drop a batch the remote rejects`
  - `ProductEventSender should keep the queue when the remote does not receive it` (replaces
    `ProductEventSender should keep the queue when the remote throws`)
- `NativeStorageTelemetryRepositoryTest`
  - `NativeStorageTelemetryRepository should drop the oldest product event when 1,000 are queued`
- `RemoteTelemetryRepositoryEndToEndTest`, tagged `telemetry-collector`
  - `RemoteTelemetryRepository should have the sample batch accepted by the telemetry collector`
  - `RemoteTelemetryRepository should have a batch of 101 product events rejected by the telemetry collector`
  - `RemoteTelemetryRepository should have a batch sent again accepted by the telemetry collector`

## Known limitations

- Anyone can send made-up product events: the origin only stops other web pages, not scripts.
- Two product events with the same name, install id and session id in the same millisecond are kept once.
- Over 100,000 rows written in a day, about 33,000 product events with the index, D1 fails until 00:00 UTC, and the
  product events stay queued on the devices.
- Withdrawing the consent erases nothing already received: the retention period deletes it.
- The product events are read in SQL only: charts are another plan.
- The rate limit of 60 requests a minute is counted by each Cloudflare location, so it is approximate.
- The JVM build never sends, as only GitHub Pages is played by others.
- Every player who accepted the privacy notice version 1 is asked again.
