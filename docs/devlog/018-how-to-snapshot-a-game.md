---
date: 31st October 2026
commit: 3c0cbd4c77cc88c6be901cdbe78d458385f6932e
---

# 018 — How to Snapshot a Game

## Hook

I walk the knight's party across the map. I recruit the elder, buy a health potion, win two battles.

Then I close the window.

All of it, gone. Play new game starts again from the deployment in the resources.

> Where does a game actually live?

In memory, spread across a dozen subdomains. To save it, I had to decide who writes it down.

**Whoever owns the state owns its snapshot. And a save is a contract with the future.**

## Context

[The open world](014-open-world.md) and [the economy](015-game-economy.md) gave the player progress worth keeping:
recruits, items, gold, defeated enemies waiting to respawn.

And [the architecture redesign](016-architecture-redesign.md) had just untangled the subdomains. Each one owned its own
state, and nothing else. That turned out to matter.

I kept the scope tight:

- **Only while exploring.** A new Start button opens a pause screen, and only shows while exploring. So the battle and
  the CPU battle brain hold no state to save.
- **Manual, 3 slots.** No autosave after each battle: I didn't want it. Three fixed slots, each showing the date, the
  leader and the party size.
- **No migrations.** A save of another version is "Incompatible": it can be deleted, not loaded.

## Main Story — Deciding What a Save Is

### Replay the events?

The game already runs on [an event bus](006-how-to-build-a-type-safe-eventbus-for-korge.md). Every deployment, move,
cast and purchase is an event.

The tempting idea: record them, and replay them on load.

I dropped it. A replay only works if the rules are exactly the same. Change one ability's damage in a TOML file, and
every replayed battle goes differently. Every save breaks on any balance tweak. And the file grows with playtime.

**Events describe how you got here. A save only needs where you are.**

### One game-state aggregate?

The opposite idea: one big `GameState` object that knows every subdomain, and serialises itself.

I dropped that too. One class tied to every subdomain, right after I spent a whole entry cutting those ties.

### Each subdomain snapshots itself

So the final approach: a small `Snapshottable` port in `shared.domain`.

```kotlin
interface Snapshottable {
    val key: String
    fun snapshot(): String
    fun restore(snapshot: String)
}
```

Each of the 7 subdomains holding runtime state implements it, and writes its own JSON. The `save` subdomain just puts
them under their keys:

```text
battleUnit · battlefield · npc · lootDrop · inventory · party · cpuWanderBrain
```

`save` never knows what's inside. It depends on no other subdomain.

Only runtime state is saved. Terrains, abilities, units and the map are read from TOML again. On load, the scene runs
the first 10 setup steps, terrains to buildings, then restores the snapshots in order, silently, with no creation
events. Replaying those would create empty inventories and new wander brains.

Then one `GameRestored` event, and every presenter redraws the whole state.

### A contract, pinned by a test

Every save holds a `SAVE_VERSION`. And a golden fixture guards it: `SaveFixtureTest` plays a game with nothing random,
and checks it is still saved exactly as `v<SAVE_VERSION>.json`. Loading it must put the knight back on its tile, with
its health potion and 40 gold. And the older `v0.json` must be refused.

It paid off quickly. When the abilities cap moved onto the battle unit, its snapshot changed shape, the fixture failed,
and `SAVE_VERSION` became 2. CI caught it, not a player's save.

### The first version had a smell

It worked. But each `Snapshottable` was doing too much.

It read its repository, checked that every id still existed in the other subdomains, wiped the aggregates and recreated
them. That's business logic, sitting in an adapter.

So in a second phase, each saved subdomain got a `Search…Snapshot` query and a `Restore…` command. The `Snapshottable`
only maps them to JSON. Two architecture tests lock `save` in both directions: it depends only on `shared`, and only
`screen` imports it. A third one lets only the use cases call a repository.

One more idea came up there: let each repository register itself for saving through the event bus. I dropped it. The
bus is FIFO and its handlers return `Unit`, so it can't hand back a snapshot. And the restore order would be whatever
order they subscribed in.

The saved JSON didn't change. The fixture proved it.

### Tradeoffs

- **No migrations.** Any change to a snapshot's shape makes every older save incompatible. Fine now. Not after a
  release.
- **Random rolls aren't saved.** Reloading rolls anew, so retrying a fight until it goes your way works.

## Key Takeaways

- **Whoever owns it, snapshots it.** Each subdomain writes its own JSON, so `save` never knows the shapes and depends on
  nothing.
- **A save is a contract.** Version it, and pin it with a golden fixture, so a shape change fails CI instead of a
  player's save.
- **Adapters map, use cases decide.** Checking ids and wiping aggregates is business logic, even inside a save adapter.

## What's Next

Right now, a save survives only until the next shape change.

That's fine while I'm the only player. It won't be once anyone else plays. Next is keeping saves alive: migrations, so
an older version is upgraded instead of refused.

A contract you can never change isn't a contract. It's a trap.
