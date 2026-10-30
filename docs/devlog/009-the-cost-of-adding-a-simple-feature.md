---
date: 5th October 2026
commit: 74d8cdb1e0b6334438bebad948a204bff26bf389
---
# The Cost of Adding a “Simple” Feature

## Hook

Adding a terrain that heals a unit sounds like a small feature.

Create a new terrain, give it a healing effect, and you're done.

At least, that's how it looks from the outside.

In practice, adding terrain effects touched the battlefield domain, effect application, battle units, terrain rendering, tests, and eventually the CPU's decision-making.

What started as a simple idea—**“some tiles should affect the units standing on them”**—became a useful reminder that the cost of a feature is often determined by how many systems need to understand it.

## Context

The battlefield in my tactical turn-based game is a 2D grid of tiles. Each tile references a terrain, and terrains already had properties such as whether they could be occupied.

I wanted terrain to become more than something that determined how a tile looked or whether a unit could stand there.

The goal was to make positioning itself part of the strategy.

A unit shouldn't only ask:

> “Can I move there?”

It should eventually also ask:

> “What happens if I move there?”

The first step was therefore to make terrain capable of having an effect.

I deliberately kept the initial scope small. A terrain could have either no effect or one effect, and the effect would be applied to an occupying battle unit at the beginning of the next round.

At this point, however, I still hadn't added any new gameplay terrain. I was building the infrastructure that would make the mechanic possible.

That distinction turned out to be important.

## Main Story

The first challenge was making terrain an effect source.

The existing effect system already understood effects originating from battle units. Terrain introduced another possibility: the source of an effect could now be the terrain itself.

Instead of creating a separate mechanism for terrain damage or healing, I extended the existing effect flow so that it could represent different sources.

The battlefield could then identify occupied tiles whose terrain had an effect and request that the corresponding effect be applied to the occupant when a new round started.

This meant the new mechanic could reuse the existing effect system instead of creating a parallel implementation just for terrain.

But the work didn't stop at the domain.

I also needed actual terrain to exercise the system.

The battlefield setup had originally been a 10×10 grid. I expanded it to 16×16 and introduced grass alongside the existing sand and void terrains.

That immediately exposed another problem.

The grass itself was displayed correctly, but the adjacent sand-to-grass transition tiles were not. The battlefield uses Wang tiles to display terrain transitions, so the problem wasn't with the grass terrain itself—it was with how the transition was being calculated or displayed.

I created a test to reproduce the problem and fixed the underlying issue before continuing.

This was a useful reminder that adding content can expose problems in systems that were already considered “finished.”

Once the terrain system and transitions were working, I could finally use terrain effects to create actual gameplay.

I added water with a healing effect and lava with a damaging effect.

But now another system had to understand the feature.

The CPU.

In my previous work on the CPU brain, I decided to build the opponent early because random CPU behavior made it impossible to properly evaluate whether the combat system was fun.

The CPU uses Utility AI to evaluate possible actions and movement destinations. Its `WhereShouldMove` query already considered several factors when deciding where a unit should move.

It didn't know anything about terrain effects.

That meant the CPU could evaluate two destinations without considering that one might heal the unit while another could damage it.

Adding the terrain mechanic had therefore created a new requirement for the CPU.

I didn't want to solve this with terrain-specific rules.

The CPU shouldn't need to know:

> “Water is good.”

or:

> “Lava is bad.”

Instead, it should understand the consequence of the terrain.

I added the concept of `HealingNeed`, a normalized value between `0.0` and `1.0`, calculated from the unit's current and maximum health.

A healthy unit has little need for healing. A damaged unit has a much greater need.

That value can then influence the utility of a terrain's health effect.

Conceptually:

```text
terrainHealthUtility =
    healingNeed * healthEffect
```

The important part is that this becomes another input to the existing movement decision rather than a completely separate decision system.

A healthy CPU unit doesn't suddenly abandon its tactical position because there is a small amount of healing available somewhere else.

A badly damaged unit, however, has a much stronger reason to seek healing terrain and avoid damaging terrain.

The same mechanism also works with different effect magnitudes. A terrain that heals for 10 health can be more desirable than one that heals for 2, while a terrain that causes 10 damage can be more dangerous than one that causes 2.

The CPU doesn't need to know what the terrain is called.

It only needs to understand what the terrain does.

That's the part that makes the feature more valuable than simply adding two new terrain types.

## Key Takeaways

- A seemingly simple gameplay feature can cross many architectural boundaries.
    
- Reusing the existing effect system is preferable to creating terrain-specific effect logic.
    
- Adding new content can reveal bugs in systems that previously appeared complete, as happened with the terrain transitions.
    
- New player-facing mechanics can create requirements for the CPU that aren't immediately obvious.
    
- Designing the CPU around the consequences of a mechanic rather than specific terrain identities keeps the system extensible.
    

## What's Next

Water and lava are only the first examples of terrain affecting combat.

The more mechanics I add, the more systems need to understand the battlefield and its consequences.

That makes architecture increasingly important.

A feature isn't really finished when I can make it work in isolation. It is finished when the other systems that interact with it can understand it without requiring another collection of special cases.

For a tactical game, the battlefield is becoming part of the gameplay itself.

And that means every new rule I add to it has a cost—not necessarily because the rule is complicated, but because **more of the game needs to understand what that rule means.**