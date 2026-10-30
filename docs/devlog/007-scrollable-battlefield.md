---
date: 23rd September 2026
commit: 2a671c99fd1f4e8dfed0d1326f17063f6ad226f5
---

# 007 — Scrollable Battlefield

## Hook

I wanted a bigger battlefield.

Not a huge one. Just 10x10 tiles instead of 8x8.

The problem was the screen. The battlefield had room for exactly 8x8 tiles of 48px. Two more rows and two more columns
simply didn't fit.

> Do I shrink the tiles, or do I let the board scroll?

I chose to scroll. And the first version took two small iterations, less than an hour apart.

**The smallest version that works is the right first version, as long as it leaves room to grow.**

## Context

At that point the game was still a combat prototype. A knight against a few rats, on a small board, with abilities and
effects already working.

The whole battlefield was always on screen. What you saw was all there was.

I had two goals:

- **Room for tactics now.** A bigger map gives space to move, to flank, and to line enemies up for chained effects. A
  tight 8x8 board doesn't.
- **No screen limit later.** Terrains, transitions and handmade maps were coming. The size of the screen shouldn't
  decide the size of a battle.

And I deliberately left a lot out:

- No automatic camera. Only the player moves the view, by dragging it.
- Every tile is created, even the ones off screen. For 10x10, that's fine.
- No zoom, no wheel, no inertia after releasing the drag.
- Mouse drag only. No keyboard, no edge scrolling.

## Main Story — A Window Onto the Map

### Why not shrink the tiles?

Shrinking the tiles was the obvious alternative. 10x10 tiles of about 38px fit in the same space.

It would have worked once. But the art gets harder to read, and the next bigger map shrinks it again.

Scrolling changes the question instead. The battlefield stops being the screen and becomes a window onto a bigger map.

### Drag and drop

The first iteration did exactly that, in three pieces:

```text
BattlefieldView (fixed 384x384)
   │
   ▼
viewport  (clipContainer, 8x8 tiles visible)
   │
   ▼
battlefieldGrid  (10x10 tiles, 480x480, moves inside)
```

The viewport has a fixed size: `TILE_SIZE * VISIBLE_TILES`, so 48 * 8 = 384px. It clips whatever is outside it.

The grid has the size of the map, and moves inside the viewport.

Dragging moves the grid, clamped so that the map never leaves an empty gap at its edges:

```kotlin
viewport.onMouseDrag { event ->
    val minX = -(mapWidth - VIEWPORT_WIDTH).toDouble()
    val minY = -(mapHeight - VIEWPORT_HEIGHT).toDouble()

    battlefieldGrid.x = (battlefieldGrid.x + event.dx).clamp(minX, 0.0)
    battlefieldGrid.y = (battlefieldGrid.y + event.dy).clamp(minY, 0.0)
}
```

That's the whole camera on day one: a clip, a drag and a clamp.

It worked.

### Making it feel right

But it felt stiff. Setting `x` and `y` on every drag event makes the board snap to the pointer, frame by frame.

So in a next iteration, the grid glides there instead:

```kotlin
battlefieldAnimator
    .cancel()
    .moveTo(
        view = battlefieldGrid,
        x = { newX },
        y = { newY },
        time = 300.milliseconds,
        easing = Easing.EASE_OUT_QUAD,
    )
```

Each drag event cancels the previous glide and starts a new one, so the board eases to a stop when released.

Making it work took one iteration. Making it feel right took another.

### Where I want it to go

For now, only the player moves the view. In the future, I want the camera to follow the action: to show what happens
outside the viewport during a battle, and to follow the party while exploring. Both would move the same grid, inside the
same clipped viewport.

I'd also like to create a map builder, and make the game open world, without limiting the size of a map to a fixed
value.

Creating every tile won't scale to that. But the viewport already knows what is visible, so it's the natural place to
decide which tiles need to exist.

### What's still missing

Input is the same on every platform. The drag works on the desktop JVM build and in the browser on a phone, but nothing
uses what each platform is good at: no keyboard shortcuts on the desktop, nothing touch specific on mobile.

## Key Takeaways

- **Build the smallest version first.** A clip container, a drag and a clamp were enough for a 10x10 map.
- **Separate the view size from the map size.** Once the viewport is fixed and the map isn't, the screen no longer
  decides how big a battle can be.
- **Let the first version leave room to grow.** A fixed viewport and a grid moving inside it are where a camera and
  bigger maps can plug in later, without replacing them.

## What's Next

The day before, the battlefield had got its first two terrains: sand and void.

On a bigger map, the hard edges between them looked wrong. The borders needed to look natural, and that turned into [Wang auto-tiling](008-wang-auto-tiling-battlefield.md).

The principle stays the same: **start with the smallest thing that works, and let the game tell you where it needs to
grow.**
