# Plan: Battlefield tiles without a grid

> **Status:** done.
>
> **Summary:** The battlefield looks like a grid: a dark line runs along the borders of every tile, over the terrain.
> It is not drawn on purpose. Each tile is a KorGE `UIButton`, whose background casts a 10px shadow by default
> (`elevation`). The tiles are drawn row by row, so each tile's shadow falls on the borders of the tiles drawn before
> it. Instead, the tiles cast no shadow, so the terrain of neighbour tiles joins seamlessly.

## Decisions

| Topic         | Decision                                                                                                                                                          |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Fix           | `elevation = false` on each tile (`BattlefieldView.createTile`). The tiles stay `UIButton`s, so tapping a tile is unchanged.                                       |
| Map builder   | It shares the `BattlefieldView`, so its tiles lose the lines too. A grid to help painting maps, if wanted, is a separate feature.                                  |
| Other buttons | Unchanged: the inventory slots, the bag button and the ability buttons keep their shadow.                                                                         |
| Terrain art   | Unchanged: the notches along the edges of the sand corridor are drawn in the terrain transition tiles, not by the view.                                           |
| Test          | `BattlefieldViewTest` checks the displayed tiles cast no shadow. The change was checked with a game snapshot, see [How to take a game snapshot](../testing/how-to-take-a-game-snapshot.md). |
