
# 004 — Improving the workflow to add new effects and abilities

## Hook

Adding a new ability should feel like designing a piece of the game, not like performing archaeology on the codebase.

That was not how it felt during the previous iteration.

The last ability I added was one where, when a battle unit was defeated, venom was applied to its adjacent allies. Those allies would then be affected by the venom at the beginning of each turn, losing health over time.

The ability worked, but implementing it exposed several problems in the domain model. Before adding the next ability, I decided it was time to stop working around those problems and rethink the workflow itself.

The next ability would be **Summon Unit**. It turned out to be a good test for the new approach.

## Context

Abilities and their effects are going to be one of the core content-generation mechanisms of the game. The main combat mechanic relies heavily on effects interacting and creating chains, so I expect to need a large number of effects and abilities.

That means the cost of creating content matters.

I want to eventually make the project open source and attract other contributors, so this is also a scalability problem for the development process. I shouldn't have to explain the entire battle domain every time someone wants to add an ability. Ideally, the code and documentation should make the intended workflow discoverable.

The previous model wasn't there yet.

## Main Story

There were actually three related problems.

First, the targeting model initially assumed that an ability affected a single enemy or tile. I had since changed it to support groups, so an ability could be cast over all the tiles in a selected group. That was already pushing the original assumptions of the domain.

Second, the responsibilities between abilities, effects, and battle units weren't clear enough.

The timing of an effect could be defined by the ability, while the effect itself implicitly contained rules about who should receive it. For example, an effect could effectively mean “apply X to a nearby ally.” The target wasn't really a domain concept yet; it was hidden inside the effect type, sometimes resulting in long effect names that encoded too much information.

This led to special rules during resolution: if the effect was one type and the ability application another type, do something different from the normal behaviour.

The third problem was even more restrictive: all effects of an ability were effectively applied to the same target.

That meant I couldn't naturally express something like:

```text
Blood Sacrifice

Target:
    Enemy

Effects:

    Damage(10) → SelectedTarget
    Damage(5)  → Caster
```

The selected target was being treated as though it were the target of every effect.

The solution was to make an implicit concept explicit: **effect application**.

The new model separates three things:

- **Ability** — defines targeting and a collection of effect specifications.
- **EffectSpec** — describes which effect should happen and how its target is resolved.
- **EffectApplication** — is the concrete result, containing the source, target, and effect.

The important rule is:

> **An ability's selected target is input to the ability execution, not necessarily the target of every effect.**

An ability execution receives the caster, selected target, and battle state. It then resolves its effect specifications into concrete effect applications.

That distinction turned out to be surprisingly powerful.

Now the selected tile or group can be one input to the ability without forcing every effect to affect it. One effect can target the selected enemy, another can target the caster, and another can resolve a group of nearby units.

It also made the domain easier to read. What was previously implicit now has a name and its own place in the model.

There was a cost: there are more concepts to understand. But the concepts now have clearer boundaries and the use cases have clearer responsibilities.

The difference became obvious when I implemented **Summon Unit**.

Summoning doesn't really have an enemy target. It has a caster and a selected tile, and its effect is to deploy a new battle unit into the battlefield. This was a useful test because it required the ability to reference new domain elements rather than simply modifying an existing enemy.

With the new EffectSpec model, adding it was straightforward. I no longer had to read through every related battle-unit use case to understand which hidden path I might be affecting. There was a much smaller, clearer subset of the domain that needed to change.

Writing ability definitions also started to feel almost like writing a small DSL. Instead of encoding several responsibilities into a long effect type name, I could express the different decisions separately.

When I found myself writing those long names in the old model, I could feel that something was wrong. The new model made the separation explicit.

### Making the workflow repeatable

The domain refactor solved the architectural problem, but there was another problem: **how do I make this workflow repeatable?**

I added a how-to guide for creating new effect outcomes. The guide describes what needs to be considered, how the concept is represented in the domain, and provides a checklist so important steps aren't accidentally skipped.

I'm planning similar guides for EffectSpecs, ability applications, and effect applications, along with an end-to-end reference showing how the user's action travels through the battle system. I also want to keep using event-storming diagrams to make the flow easier to visualize.

I'm following the Diátaxis framework for organizing this documentation, separating procedural how-to guides from reference material and other types of documentation.

This isn't just about future contributors. I'm a solo developer today, but future me is also a contributor who may return to this code after several weeks and have forgotten the reasoning behind it.

Code can show what the system does. It doesn't always show why it was designed that way.

For an open-source project, making that reasoning easier to recover is part of making the project easier to contribute to.

## Key Takeaways

- When a workflow starts hurting, don't just optimize the workflow—**question the model underneath it**.
- Making an implicit concept explicit can add a domain concept while still making the overall system simpler.
- An ability's selected target shouldn't necessarily determine the target of every effect.
- Good content-generation workflows need both clear domain boundaries and documentation that captures the intended process.
- Designing for future contributors also means designing for your future self.

## What's Next

The new workflow is working well, but the model isn't finished.

There are still events that aren't listened to and may require additional use cases, such as negating health increases or movement.

For now, though, adding new abilities feels less like fighting the domain and more like expressing game design through it—which is exactly what I wanted from the system.