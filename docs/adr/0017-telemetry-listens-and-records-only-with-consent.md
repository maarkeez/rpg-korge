# Telemetry listens to the other subdomains and records only with consent

Telemetry is a subdomain that listens to the domain events of the others and records product events through its
repository, `FacadeTelemetryRepository`, over `NativeStorageTelemetryRepository`, keeping its consent and ids, and
`RemoteTelemetryRepository`, recording its product events, which was left empty until we chose how and where to store
them: see [ADR 0018](0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md). Only `screen` and
`gameSetup` know it exists, publishing its request events for what no domain event says (the main menu opened, a new
game started). It records nothing without the player's consent: only once consent is given does it write
the install id on the device and a game id in the saves, and withdrawing it deletes the install id. We chose listening
over each subdomain recording its own product events so the rules of the game never change for telemetry, and consent
first because EU law (ePrivacy and GDPR) forbids storing analytics ids on the player's device without it.

Unlike the other subdomains, which store the current state of their aggregate roots and publish their events on the
event bus, telemetry stores its consent and ids as state and appends its product events to a log that is never read
back: saving a `Telemetry` pulls its `ProductEventRecorded` events and queues them on the device, and a sender sends the
queue to `RemoteTelemetryRepository` in the background, removing what it accepted and trying again at the next launch.
We chose this over publishing them for a remote adapter subscribed to the bus, as a product event is a fact to keep, not
one another subdomain reacts to (the events it comes from are already on the bus), and over sending them as they are
saved, as a command on the game loop must not wait on the network, nor lose an event when it fails or the game closes.
It is not event sourcing: no state is rebuilt from them.

## Consequences

- A product event can only be tracked when a domain event or a telemetry request event says it happened.
- Saving the game id bumps `SAVE_VERSION`, making every earlier save incompatible.
- Choosing the storage bumps the version of the privacy notice, so every player is asked for consent again.
- Declining the consent, or a new version of the privacy notice, deletes the product events still queued, so each is
  only sent under the consent it was recorded with.
- Until the storage is chosen, the empty `RemoteTelemetryRepository` accepts every product event, so none is kept on
  the device; choosing it also makes the privacy notice say that declining deletes the ones still waiting. Both
  happened with [ADR 0018](0018-product-events-kept-by-our-own-telemetry-collector-on-cloudflare.md).
