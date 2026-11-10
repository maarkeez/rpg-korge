# The event bus handles events in the order they are published

`InMemoryEventBus` queues every event published, and `dispatch()`, on each frame of the game, handles them first in,
first out, until the queue is empty. An event a handler publishes waits behind the ones already queued. We chose this over
dispatching each event as it is published, depth-first, so that a handler never runs in the middle of another
subdomain's command, and so a subdomain orders what happens next by the order it publishes in. It is hard to reverse,
as these orders hold only while the bus is first in, first out:

- `BattleUnit`, `Inventory` and `Npc` publish each of their events followed by the battlefield request it needs (e.g.:
  occupying the tile a battle unit was deployed on), so the battlefield is up to date before anything reacts to their
  next events.
- `RecruitNpc` takes the npc off its tile before the party recruits a battle unit on it, and `RecruitMember` requests
  the battle unit to be deployed before it joins the party.
- `ApplyStandingTerrainEffects` requests the terrain effects before `ExplorationSecondStarted`, so the battle units
  that move during the new second (e.g.: wandering) count as moved in it.
- `ApplyEffect` deploys a summon as the other effects of the cast are applied, so its tile is occupied before the
  cast is resolved.
- `RequestEffectApplicationToCastTargets` publishes `AbilityCastResolved` after every `RequestApplyEffect` of the cast,
  so it is handled once all of them have been applied.
- `RequestQuestReward` runs on `QuestCompleted`, after the requests already waiting on the bus, so a leader that has
  just moved is on its new tile, and a leader that has just been defeated has left the party.
