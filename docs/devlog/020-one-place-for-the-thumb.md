---
date: 3rd November 2026
commit: 530c5a7df91fa598182eb6f775967a4d7c050b5f
---

# 020 — One Place for the Thumb

## Hook

Open the quest journal. Back is a small button, down in the left corner.

Open a question, like Quit to the menu. Its buttons sit higher up, around the middle of the panel.

Open the shop. Back and Buy share a line with the item's name.

Start a battle. Finish turn is lower than all of them, across the whole screen.

> Where do I tap?

It depends on the view. And the game is made for a phone.

**Every panel had a good place for its buttons. Together, they gave a thumb no place to go.**

## Context

The panels arrived one at a time, and each placed its buttons wherever they fit. [The Storybook](017-storybook.md) shows
each view alone, and alone, each one looked fine.

What exposed it was a new feature: giving gold.

[The economy](015-game-economy.md) gave each battle unit its own gold, and no way to move it. A knight with 20 G can't buy
the 50 G hammer while the viking beside it carries 100 G.

So the gold became a button, opening a panel with a slider. In the prototype, Cancel, All and Give sat inside it, like
every other panel's buttons.

I moved them down, to the level of Finish turn. The room the panel gained went to the portraits.

It looked right. And it made everything else look wrong.

So I set a rule: every action of a panel sits in one row, at the level of Finish turn. That covered seven views: the
journal, a quest's detail view, the questions, the save slots, the shop, the formation and the merchant.

Some buttons stayed out:

- **The options of pause and the main menu.** Moving them would leave those panels empty.
- **The bag's Close.** The row holds the selected item's actions by then.
- **The shop's buyer arrows and a save slot's Delete.** They stay beside what they act on.

## Main Story — One Row, Seven Views

### Sharing the width

A Back of fixed width leaves part of the row empty. So the buttons share the whole width equally:

```text
[             Back             ]
[     Back     ] [   Abandon   ]
[ Leave ] [  Sell  ] [   Buy   ]
```

A lone button fills the row. And the first one always starts on the left, so Back doesn't jump when stepping out.

### The hole left behind

Taking the buttons out of a panel leaves a hole.

> What goes there?

I prototyped four answers:

- **A longer description.** It left a blank band on most quests.
- **Shrink the panel.** It left an empty gap above the row.
- **Grow the panel.** It added almost nothing.
- **Spread.** The panel keeps its height, and evens out its gaps.

Spread won. The journal now shows 4 quests instead of 3, and the merchant's line of dialog got more room.

### Who owns a button?

Before, each panel drew its own buttons and told a delegate when one was tapped. Now a panel only displays its content.
The presenter owns the actions, and hands them to the row:

```kotlin
playerCallToActionView.displayActions(detailActions(quest))
```

A tap doesn't dismiss the row. The presenter displays what follows.

The row itself is 45 lines. **The view was the small part: moving every panel onto it took three milestones, of about 30
files each.** More than half of them were tests and stories.

### Same place, same meaning

My plan said every button keeps its colour. That didn't last.

With everything in one row, colour is all that tells the buttons apart. And Buy, the main action of the shop, looked
exactly like Back. So the merchant's row became Leave, Sell and Buy, with Buy in blue, as Give and Finish turn are.
Consume, Equip and Unequip followed. And Discard now asks first, in a row of its own, in red:

```text
Grey   the default         Back, Leave, Sell
Blue   the main action     Finish turn, Give, Buy, Consume, Equip
Red    can't be undone     Abandon, Quit to the menu, Discard
```

### The price

Tap Abandon, and its question opens with Back and Abandon in the same places. Tap twice, and the quest is gone, unread.

I could ignore taps for a moment, or swap the sides, but swapping breaks Back on the left. **The quest's giver can start
it again, so I left it.**

## Key Takeaways

- **Look at views together.** Alone, each one looked fine.
- **Let one good choice spread.** A good decision in one view is a question for every other view.
- **Move the action, plan the space.** Taking buttons out leaves a hole, and filling it is the real decision.
- **Colour needs a meaning.** The place says where to tap, and the colour says what happens.
- **Consistency has a price.** When every button sits in the same place, the next one is under your thumb too.

## What's Next

The buttons agree now. The rest of the screens may not.

Titles, hints and the layout inside each panel grew the same way the buttons did, one view at a time. Next is a wider
pass: put them side by side, and look for what differs without a reason.

A player should only have to learn a screen once.
