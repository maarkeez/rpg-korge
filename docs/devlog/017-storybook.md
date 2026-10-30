---
date: 30th October 2026
commit: 8ad1d1125014f03e013c0ac0670986ede1f521c4
---

# 017 — Storybook: Every View, in Every State

## Hook

> What does the shop look like when the bag is full and the buyer has no gold?

To find out, I had to start the game, walk to the merchant, loot until the bag was full, spend the gold, and open the
shop.

Then change one pixel, and do it all again.

Some states I never saw at all: a disabled button, a party of one. They existed in the tests, but nobody looked at them.

**A link to a view in a given state is something I, a coding agent and an artist can all look at.**

## Context

The views of the game were already tested. Each one has view tests with page objects, and game snapshots cover whole
scenes.

But a test tells me a button is disabled. It doesn't tell me it _looks_ disabled.

[The shop](015-game-economy.md) made that painful. It has modes, buyers, full bags, empty wallets, and a reason on every
disabled button. Each state was a few minutes of playing away.

Web developers solved this long ago with [Storybook](https://storybook.js.org): every component shown in isolation, in
each of its states. I wanted the same for the game.

I kept the scope clear:

- **Views, not scenes.** A whole scene needs the full domain state, and snapshots already cover it.
- **Live KorGE, not images.** The real views, renderer and input, on the JVM with autoreload and in the browser.
- **No visual regression.** Comparing screenshots automatically could wait.
- **Storybook's own words.** Component, Story, Args, Controls, Actions, Globals. Collaborators already know them.

## Main Story — From One Health Bar to Every View

### A static gallery, or a live app?

The cheapest option was a gallery: render every view to an image, and browse the images.

I dropped it. An image can't be tweaked: I want to drag a control and watch the health bar empty. And a gallery goes
stale the moment a view changes.

Whole scenes were the other temptation. A battle scene needs a battlefield, a party, a CPU brain. That isn't a story,
it's a game.

### First iteration: a skeleton and a link

The first iteration was small on purpose: a new `storybook` subdomain, a sidebar listing the components, a phone frame
in the middle, and one component: the health bar, full, half and empty.

The game and the Storybook ship in the same bundle. On the web, the `/storybook/` folder picks the Storybook.

**Every story has its own URL**, with the args changed from its defaults and the language:

```text
?story=screen-shop--sell&args=mode:SELL;buyerGold:0&globals=language:es
```

Opening it shows exactly that shop, in Spanish, with no gold. I can send it to an artist. A coding agent can open it with
Playwright, take a screenshot, and look at what it just changed.

The next iterations added the Controls, to edit the args live, and the Actions, to log every callback of the view.

### Second phase: 38 views to zero

From the start, an architecture test required every view to have its stories. But 38 views had none, so they were
listed as exceptions.

**The list could only shrink**, as the dependency allowlist in [the previous entry](016-architecture-redesign.md).

I emptied it area by area: bars and badges, buttons and dialogs, the map builder, the battle HUD, the battlefield.

The last areas fought back. Five views took domain objects, such as the npc dialog taking an `Npc.Dto`, which a story
can't build without the domain. So each moved to plain view data, holding only what it draws.

`BattlefieldView` was worse: terrain, transitions, highlights, flashes and units, all in one. I split it by layer, and
each layer got its own stories.

Then the list was deleted, and a new rule took its place: a view may not import the domain.

### Third phase: using it

On a phone, the fixed 1280x900 layout was tiny, so it got a zoom bar, a pinch and panning. With around 178 stories, it
also got a search box.

Then the first real payoff. Disabled buttons looked enabled. I tried two looks, faded and see-through, side by side in
the Storybook. The faded one won: the see-through one still looked tappable.

### What is still unfinished

- **Design issues, noted, not fixed.** Writing stories exposed them: a thin line across some attack preview bars, a
  black rat that hardly stands out over the void.
- **Stories to maintain.** Every new view needs stories, by rule. That's more work per view.
- **A fixed desktop layout.** On a phone, it is zoomed, not laid out for it.

## Key Takeaways

- **Isolation shapes the view.** Needing a story pushed the views onto plain data, and split the battlefield by layer.
- **A link is shared eyes.** A URL per story is something anyone, or any agent, can open.
- **Shrink the allowlist.** Start a rule with its exceptions listed, then remove them until it needs none.
- **Compare, don't imagine.** Two looks side by side decide faster than guessing.

## What's Next

The Storybook shows every view clearly. Including the art inside them.

And the art is the weakest part. Some sprites are placeholders, some don't match each other, and a black rat vanishes
over the void. Improving the pixel art is a long, pending topic, and it is next.

Seeing a problem is the first step to fixing it. Now I can see all of them.
