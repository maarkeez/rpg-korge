# The event bus handles events in the order they are published

`InMemoryEventBus` queues every event published, and `dispatch()`, on each frame of the game, handles them first in,
first out, until the queue is empty. An event a handler publishes waits behind the ones already queued. We chose this over
dispatching each event as it is published, depth-first, so that a handler never runs in the middle of another
subdomain's command, and so a subdomain orders what happens next by the order it publishes in. It is hard to reverse,
as these orders hold only while the bus is first in, first out:

- `BattleUnit` publishes each of its events followed by the battlefield request it needs (e.g.: occupying the tile it
  was deployed on), so the battlefield is up to date before anything reacts to its next events.
- `ApplyEffect` deploys a summon as the other effects of the cast are applied, so its tile is occupied before the
  cast is resolved.
- `RequestEffectApplicationToCastTargets` publishes `AbilityCastResolved` after every `RequestApplyEffect` of the cast,
  so it is handled once all of them have been applied.
- `RequestQuestReward` runs on `QuestCompleted`, after the requests already waiting on the bus, so a leader that has
  just moved is on its new tile, and a leader that has just been defeated has left the party.
