# Product events are kept by our own telemetry collector on Cloudflare, for 3 months

The telemetry collector is a Cloudflare Worker on the free plan, at a `workers.dev` address, keeping product events in
a D1 database restricted to the EU jurisdiction, and deleting every product event older than the retention period, 3
months, with a nightly scheduled run. It keeps only what the game sends and when it was received, never the IP address,
browser or country of the request. We chose it because the game is a free, open pet project that must cost nothing to
run, we want to ask our own questions in plain SQL, and a short retention period keeps the least data for the shortest
time, and makes changing the telemetry collector cheap: the old one empties itself within 3 months, so there is nothing
to migrate.

## Considered options

- **PostHog Cloud (EU)**: funnels, retention charts and SQL with no backend to write, 1 million product events a month
  for free, but its free plan keeps them for a fixed year, which nothing on it can shorten.
- **Aptabase**: privacy first and made for apps and games, but its free plan stops recording after 20,000 product events
  a month, about 500 sessions.
- **Supabase**: Postgres the game could write to directly, but its free projects are paused after 7 days without
  activity, and stay paused until resumed by hand.
- **Cloudflare Workers Analytics Engine**: deletes after exactly 3 months by itself, but stores every product event in a
  fixed shape of strings and numbers, its SQL has no joins, it cannot be kept in the EU, and it is free only until
  Cloudflare starts billing it.
- **Self-hosting** Umami, Aptabase or ClickHouse on an always-free virtual machine: free, but a server to keep running and
  patched.

## Consequences

- The telemetry collector is written in TypeScript, Cloudflare's own language for Workers, in this repository, and
  deployed from CI whenever it changes. Kotlin/JS can build a Worker too, but with hand-written bindings for D1, and it
  only pays off once the game and the telemetry collector share the product event classes, which needs the game split
  into Gradle modules. Instead, a sample batch of product events in the repository is written by a test of the game and
  read by a test of the telemetry collector, so a change to the shape on one side breaks a test.
- Only the game deployed to GitHub Pages sends product events, so playing it locally, on the JVM or in the Storybook
  never mixes with the players'. The telemetry collector accepts requests from its origin only, which filters other
  web pages but not scripts.
- Anyone can send it made-up product events, as its address and everything the game sends are public. It only checks
  their shape and names, the size of a batch and the origin, and limits the requests of each IP address: forging them
  gains nobody anything.
- A batch it rejects is dropped on the device, as sending it again would fail again and block the ones after it, while
  one it cannot receive, as it is down or over the free plan's daily writes, stays queued and is sent again later. At
  most 1,000 product events are queued on the device, the oldest dropped first.
- Withdrawing the consent erases nothing already received: the install id is never shown to the player, so none of
  their product events can be asked for, and the retention period deletes them within 3 months.
- The privacy notice names the telemetry collector's storage, Cloudflare in the EU, and the retention period, and gives
  the repository's GitHub issues as the contact, which bumps its version and asks every player for consent again.
