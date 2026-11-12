# Telemetry listens to the other subdomains and records only with consent

Telemetry is a subdomain that listens to the domain events of the others and records product events through its
repository, `FacadeTelemetryRepository`, over `NativeStorageTelemetryRepository`, keeping its consent and ids, and
`RemoteTelemetryRepository`, recording its product events, which is left empty until we choose how and where to store
them. Only
`screen` and `gameSetup` know it exists, publishing its request events for what no domain event says (the main menu
opened, a new game started). It records nothing without the player's consent: only once consent is given does it write
the install id on the device and a game id in the saves, and withdrawing it deletes the install id. We chose listening
over each subdomain recording its own product events so the rules of the game never change for telemetry, and consent
first because EU law (ePrivacy and GDPR) forbids storing analytics ids on the player's device without it.

## Consequences

- A product event can only be tracked when a domain event or a telemetry request event says it happened.
- Saving the game id bumps `SAVE_VERSION`, making every earlier save incompatible.
- Choosing the storage bumps the version of the privacy notice, so every player is asked for consent again.
