---
date: 30th October 2026
commit: 85b237521e1f65aa51099eceeaa9ef18fc26de59
---

# 016 — Architecture Redesign: Untangling the Cycles

## Hook

> Does the battlefield know about battle units, or do battle units know about the battlefield?

The answer was: both.

And the battlefield knew about npcs, which knew about the party, which knew about npcs. The battle knew about the CPU
brain, which knew about the battle.

Every subdomain looked tidy on its own. The knots only showed up in the whole picture.

**Architecture you can't see drifts: make it visible, fix it, then make a test refuse to let it drift back.**

## Context

The game is split into subdomains: `battle`, `battleUnit`, `battlefield`, `npc`, `party`, `inventory`, `lootDrop`, the
CPU battle brain and the CPU wander brain, among others. They talk through the
[type-safe event bus](006-how-to-build-a-type-safe-eventbus-for-korge.md): one publishes an event, another listens.

That makes reaching into another subdomain cheap. Too cheap.

Each feature added more wires. [Loot](012-add-loot-mechanic.md) put items on the battlefield,
[the open world](014-open-world.md) brought npcs and a party, [the economy](015-game-economy.md) made the inventory trade
with npcs. Every slice was small, and fine.

The goal was to make the game easier to change again: touching one subdomain shouldn't drag the others along.

I kept the scope tight:

- **No behaviour change.** Same game, same order of events.
- **Production code only.** Not the tests.
- **Not a full layering.** Cycles first.

## Main Story — One Cycle at a Time

### Making it visible

On the 20th of October, I added a `dependencyReport` task. It reads the `import` lines of the production code and
draws an interactive graph of the subdomains, their cycles in red, and their fan-in and fan-out.

It was redder than I expected:

```text
9 subdomains reaching each other in one cycle
7 pairs importing each other directly
```

The pre-commit hook regenerates the report on every commit, so it can't fall out of date.

### One pattern

Every two-way dependency needed a direction. One subdomain is upstream, and it must not know about the other.

The upstream subdomain declares a `Request…` event of its own. The downstream one publishes it. The upstream subdomain
listens to its own request, and the data it needs travels inside the event, instead of being queried.

The first pair was `battle` ⇄ `battleUnit`. The battle asked battle units whether they were all defeated, and listened
to them being deployed, defeated or vanishing. Now `battle` declares three requests:

| When                                       | `battleUnit` publishes                      | `battle`                    |
|--------------------------------------------|---------------------------------------------|-----------------------------|
| A battle unit is deployed                  | `RequestJoinBattle(battleUnitId, playerId)` | Adds it to the battle.      |
| A battle unit is defeated, or vanishes     | `RequestLeaveBattle(battleUnitId)`          | Removes it from the battle. |
| ...and all their battle units are defeated | `RequestDefeatPlayer(playerId)`             | Defeats the player.         |

The battle also records the player of each battle unit, so it decides on its own when the battle is over.

Then the other six pairs, one plan and one commit each, all on the 21st of October.

### Where the request is published

My first idea was a listener on the downstream fact publishing the request. I dropped it. **The bus is FIFO**, so that
request lands behind every event already waiting. After a summon, a push or a defeat inside a CPU cast, the CPU would
decide on a stale battlefield:

> Is that tile free?

It would say yes, too early.

So the aggregate records the request right after its fact: `BattleUnit` records `RequestOccupyTile` right after
`BattleUnitDeployed`, and both are published together.

### Loot, twice

Once the pairs were gone, the report still showed one indirect cycle: `battleUnit` → `battlefield` → `lootDrop` →
`battleUnit`.

I broke it, then looked again. The battlefield only knew about loot because its tiles held the dropped items. So I moved
them to `lootDrop`: a tile now only holds its terrain and its occupant. Then I inverted the direction I had just
chosen: `lootDrop` now listens to battle units being defeated and moving.

### Guarding it

Two architecture tests keep the graph clean. `SubdomainDependencyArchitectureTest` fails on any cycle, even an indirect
one. `EventListenerArchitectureTest` keeps listeners free of business logic: they only call command use cases.

That evening, I froze what each core subdomain depends on into an allowlist, which also fails on an allowed dependency
no longer used. **The list can only shrink.**

### Tradeoffs

- **More events to follow.** A fact and its request travel together, so reading a flow means following more hops.
- **The event order shifted once.** After inverting `lootDrop`, a battle unit's move finds its loot before the
  battlefield's occupancy events. I accepted it.

## Key Takeaways

- **Make it visible first.** A generated report showed a cycle of 9 subdomains that felt fine from inside the code.
- **Order is part of the behaviour.** With a FIFO bus, where a request is published decides what the CPU sees.
- **Guard it with a test.** A test that fails on any cycle, and an allowlist that only shrinks, keep the graph clean as
  features keep landing.

## What's Next

The subdomains were untangled. The views weren't.

Next, I built a Storybook: every view of the game shown in isolation, in each of its states. And with it, a new
architecture rule: a view may not import the domain.

The same idea, applied to the UI: you can't keep clean what you can't see, and you can't keep it clean without a test
that says no.
