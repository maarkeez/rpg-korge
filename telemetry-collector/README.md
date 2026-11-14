# Telemetry collector

The telemetry collector receives the product events of the game deployed to GitHub Pages and keeps them in a D1
database in the EU for 3 months. It is a Cloudflare Worker, `telemetry-collector`, on the free plan, at
`https://telemetry-collector.tessavar.workers.dev`. Why it is built this way is in [ADR 0018](../docs/adr/0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md),
and what it does in the [telemetry collector plan](../docs/plans/telemetry-collector.md).

- `POST /product-events` with `{"productEvents": [...]}`, 1 to 100 product events and at most 64 KB, from
  `https://maarkeez.github.io` only. It answers 204 when the batch is kept, 400 when it is malformed, 403 for another
  origin, 429 over 60 requests a minute for an IP address, and 500 when D1 fails.
- Every night at 03:17 UTC, it deletes the product events received more than 90 days ago.
- It keeps the product events as the game sends them, and when they were received: never the IP address, the browser
  or the country of the request.

The shape of a batch is [the sample batch](../src/jvmTest/fixtures/telemetry/product-events.json), written by
`ProductEventBatchFixtureTest` in the game and read by the tests here, so a change on one side breaks a test.

## Running it

With Node 22, from this folder:

```
npm ci
npm test            # Vitest, in the Workers runtime with a local D1
npm run types       # the types of the bindings, after changing wrangler.jsonc
npm run typecheck
```

To send product events from the game on the JVM, which sends no origin, run it locally on port 8787:

```
npx wrangler d1 migrations apply rpg-telemetry --local
npx wrangler dev --port 8787 --var ALLOWED_ORIGIN:*
```

The [`telemetry-collector.yml`](../.github/workflows/telemetry-collector.yml) workflow runs the tests on every push that
touches it, and on `main` applies the migrations to the D1 database and deploys the Worker.

## Done by hand, once

These were done in the Cloudflare dashboard and with Wrangler, before the first deploy:

1. A Cloudflare account, and its `workers.dev` subdomain, chosen under
   [Workers & Pages](https://developers.cloudflare.com/workers/configuration/routing/workers-dev/).
2. The D1 database, created in the EU jurisdiction, which can never be changed afterwards. Its id is in
   `wrangler.jsonc`:

   ```
   npx wrangler d1 create rpg-telemetry --jurisdiction=eu
   ```

   See [D1 data location](https://developers.cloudflare.com/d1/configuration/data-location/).
3. An [API token](https://developers.cloudflare.com/fundamentals/api/get-started/create-token/) allowed to edit Workers
   scripts and D1, and to read the account settings.
4. Two secrets of the GitHub repository: `CLOUDFLARE_API_TOKEN`, the token, and `CLOUDFLARE_ACCOUNT_ID`, the account id
   `npx wrangler whoami` shows.

## Reading the product events

In SQL, with the D1 console of the Cloudflare dashboard (Storage & Databases › D1 › `rpg-telemetry` › Console), or with
Wrangler once logged in. E.g.: how many players completed Bats in the Meadow this month:

```
npx wrangler d1 execute rpg-telemetry --remote --command "
  SELECT COUNT(DISTINCT install_id) AS players
  FROM product_events
  WHERE name = 'QUEST_COMPLETED'
    AND quest_id = 'bats-in-the-meadow'
    AND received_at >= unixepoch('now', 'start of month') * 1000"
```

`occurred_at` and `received_at` are milliseconds since 1970, `occurred_at` from the player's clock.
