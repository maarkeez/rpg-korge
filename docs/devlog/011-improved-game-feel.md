---
date: 11th October 2026
commit:
---


# 011 — Improved Game Feel

## Hook (1 minute)

How do you make a tactical battle feel better without changing the underlying combat mechanics?

Sometimes, the problem isn't what the game allows players to do. It's how clearly the game communicates what's happening.

My tactical, turn-based RPG already had the core ingredients of combat: units moving across a grid, abilities targeting enemies, and chains of effects resolving during battle. But the experience still felt rough around the edges. Movement was difficult to follow, the consequences of abilities weren't always clear, and the interface made players do more mental bookkeeping than necessary.

After exploring the visual direction of the game, I started translating those design principles into practical improvements. The goal wasn't simply to make the interface look better. It was to make combat smoother, clearer, and easier to follow.

## Context

In Devlog 010, I explain how I used AI to explore the game's UI design and establish a design brief. That process helped define several principles, including tactical clarity, minimalism, and keeping the battlefield at the center of the experience.

This entry is about applying those principles to moment-to-moment gameplay.

In a tactical game, players constantly evaluate their options. They need to understand where a unit can move, which targets an ability can affect, how many actions remain, and what happens when an ability triggers a chain of effects.

If this information isn't communicated clearly, players must reconstruct the situation themselves. Even when the underlying mechanics are sound, that extra effort can make combat feel less fluid than it should.

I focused on several improvements to address these problems: movement presentation, effect feedback, camera focus, battlefield contrast, and action counters.

## Main Story — Making Combat Easier to Follow

### Showing movement and effects as they happen

One of the first problems was following the action on the battlefield.

When a unit moves between tiles or an ability triggers several effects, players need to understand not only the final result but also how the action unfolds. Without enough visual feedback, it can be difficult to follow the sequence or identify which units are affected.

I improved the presentation of movement and effects using animations and visual cues. Instead of relying entirely on the final game state, the interface helps communicate what is happening as the action resolves.

The camera also plays an important role. When significant movement or effects happen outside the visible area, the camera now moves automatically to keep those events in view.

These changes work together to guide the player's attention. Movement becomes easier to follow, the consequences of abilities are more visible, and players spend less time searching the battlefield for the action.

The underlying combat mechanics don't need to change for the experience to improve. Sometimes, presenting the same events more clearly is enough to make the game feel more responsive.

### Making available actions obvious

Another improvement concerns the battlefield itself.

The grid is where players evaluate their tactical options, but those options need to stand out from the surrounding environment. If available movement tiles and valid casting targets blend into the rest of the battlefield, players have to work harder to identify what they can do.

I increased the visual contrast between available movement tiles, valid casting targets, and the rest of the battlefield.

The intention is straightforward: players should be able to recognize their options without having to inspect every tile individually.

This is a direct application of the design principles established previously. Tactical clarity isn't just about providing information somewhere on the screen. It's about presenting the right information at the right moment, in a way that makes it immediately useful.

### Removing unnecessary mental bookkeeping

The action counters revealed a smaller but particularly interesting usability problem.

Previously, movement and casting availability was communicated through long text labels that showed only what remained. The interface didn't clearly communicate the maximum capacity alongside the remaining amount.

For example, knowing that a unit has two movement steps left doesn't tell the whole story if the player has to remember how many steps that unit started with. To understand how much of its movement has already been spent, the player must mentally reconstruct that information.

That might seem like a minor inconvenience, but tactical games repeatedly ask players to make these calculations while considering several units and possible actions.

I replaced the verbose labels with minimalistic circles.

Filled circles represent remaining actions, while empty circles with an outline represent actions already used. White represents movement steps, and gold represents casts.

There are no labels or icons. The visual language is deliberately minimal because movement and casting are repeated actions throughout combat. Once the meaning of the circles is familiar, additional text would provide little value.

This is an example of simplifying the interface without removing useful information. The player can see both the remaining capacity and the amount already spent without having to remember the original total.

### The tradeoff: clarity without clutter

These improvements share a common challenge: providing enough feedback to make combat understandable without overwhelming the battlefield.

Animations, visual highlights, camera movement, and interface indicators can all help communicate the state of the game. But adding more information isn't automatically better. Each element needs a purpose, and the combined interface must remain readable.

The approach I've taken is to make important information more explicit while keeping its presentation as simple as possible.

The camera helps players find important events. Visual contrast separates available actions from the environment. The counters communicate resource usage through a small, consistent visual pattern.

Rather than relying on one major redesign, the improvements address several small points of friction across the combat experience.

## Key Takeaways

- **Communicate the current state directly.** Good UI reduces the amount of information players must remember or reconstruct mentally.
- **Guide attention through action.** Animations, visual cues, and camera focus help players follow movement and the consequences of abilities.
- **Make tactical options immediately visible.** Available movement and valid targets should stand out from the rest of the battlefield.
- **Use minimalism purposefully.** Repeated actions can use compact visual indicators instead of verbose labels, provided their meaning remains clear.
- **Improve the experience incrementally.** Small refinements across movement, feedback, camera behavior, and counters can collectively make combat feel smoother.

## What's Next

With these improvements in place, combat feels smoother and easier to follow. The next challenge is to continue refining how players understand the broader tactical situation, beyond the action currently unfolding.

One area to explore is the effect chain preview: a clearer overview of how effects will collaborate will help players anticipate upcoming events and plan their decisions further ahead.

The goal remains the same: make the complexity of tactical combat easier to understand without taking away the decisions that make it interesting.