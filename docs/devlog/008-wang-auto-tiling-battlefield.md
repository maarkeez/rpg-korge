---
date: 1st October 2026
---

# 008 — Wang Auto-Tiling Battlefield

## Hook

At first, terrain transitions looked like a rendering problem.

We had a grid-based battlefield with different terrain types, and we wanted the borders between them to look natural instead of having every terrain tile end abruptly at its edge. Wang tiles seemed like a good fit: inspect the four neighbors, build a four-bit mask, and select the corresponding tile from a transition atlas.

It worked.

Then we discovered that making autotiling work visually was only the beginning.

Over three iterations, the problem evolved from *“how do we render terrain transitions?”* into *“who owns a terrain transition, where should it be calculated, and how do we make the whole system data-driven?”*

## Context

Terrain is its own domain in the game. It defines the different terrain types and properties such as whether a terrain can be occupied.

The battlefield is a 2D grid of tiles, with each tile referencing a terrain. Since terrain boundaries are part of the visual representation of the battlefield, we introduced transition resources using the Wang tile technique.

Our initial convention was simple. A resource such as:

```text
sand_to_void.png
```

describes the transition between sand and void.

The Wang mask uses four bits:

```text
N = 1
E = 2
S = 4
W = 8
```

So, for example, a tile with void to the east and south gets:

```text
2 + 4 = 6
```

and uses Wang tile `6` from the transition atlas.

## Main Story

### Iteration 1 — Making transitions possible

The first iteration focused on establishing the infrastructure.

We introduced explicit terrain transition definitions through resources and made the terrain domain aware of which transitions were allowed. The system scanned transition assets following the naming convention:

```text
<terrainOne>_to_<terrainTwo>.png
```

From those resources, it could discover the relationships between terrains and validate battlefields so that unsupported terrain combinations could not be created.

This was important for more than rendering. If, for example, there is no transition between sand and lava, the battlefield should not silently allow those terrains to sit next to each other.

We also introduced Wang tile selection using the four cardinal directions and moved toward automatically selecting the appropriate transition tile.

The first version solved the immediate problem, but it exposed another one: the implementation was still too tightly coupled to specific terrain assumptions.

### Iteration 2 — Making terrain data-driven

The second iteration was about improving the workflow.

Adding a new terrain should not require modifying the codebase every time. Instead, terrain definitions could live in resources such as:

```text
resources/terrain/sand.toml
resources/terrain/void.toml
```

while the transition itself would be represented by:

```text
resources/terrain/transitions/sand_to_void.png
```

The code could then infer the available transitions from the resources.

This also led to a change in how terrain occupancy worked.

Previously, a tile could be considered occupiable simply because its terrain was occupiable. But a transition can change the meaning of a tile depending on its neighbors. A sand tile next to void isn't necessarily equivalent to an isolated sand tile.

We therefore introduced a predefined occupancy heuristic based on the Wang index. When a transition occurs between an occupiable and non-occupiable terrain, some transition shapes are occupiable while others are not.

This made the battlefield domain aware of more than just the terrain identifier: the surrounding terrain could affect the state of the tile.

The system was becoming more flexible, but another issue remained.

### Iteration 3 — Who owns the transition?

The problem became obvious when looking at the same boundary from both sides.

Consider:

```text
S S S
S V S
S S S
```

The sand tile above the void sees void to its south. It therefore gets a south transition mask.

But the void tile below it sees sand to its north. If the system treats `sand_to_void` as bidirectional, it can independently produce another transition mask.

Both masks describe the same physical boundary.

The problem becomes even more apparent at corners. A sand corner might calculate:

```text
E + S = 6
```

while the neighboring void corner calculates:

```text
N + W = 9
```

These aren't two independent transitions. They are two descriptions of the same boundary.

The solution was to make transition direction explicit.

A resource named:

```text
sand_to_void.png
```

means exactly:

```text
SAND → VOID
```

It does not implicitly mean:

```text
VOID → SAND
```

Therefore, the sand cell owns the transition. The void cell simply renders as void unless an explicit `void_to_sand` transition resource exists.

This also gave us a cleaner domain model.

A battlefield tile still answers:

> What terrain am I?

But it can now also contain a `TerrainTransition` describing:

```text
fromTerrainId
toTerrainId
wangIndex
```

For example:

```text
TerrainTransition(
    fromTerrainId = "sand",
    toTerrainId = "void",
    wangIndex = 6
)
```

The battlefield domain calculates this information by inspecting the neighboring tiles. The presentation layer no longer needs to understand the battlefield's topology.

This was an important architectural shift.

Previously, `BattlefieldView` was responsible for looking at neighboring tiles and calculating the Wang index. Now the domain determines the transition and the view simply renders the result.

The flow becomes:

```text
Transition resources
        ↓
Transition definitions
        ↓
Battlefield domain
        ↓
TerrainTransition
        ↓
Battlefield DTO
        ↓
BattlefieldView
        ↓
Render
```

The adapter understands what transitions exist. The domain decides where they apply. The DTO exposes the resulting state. The view renders it.

That separation also gives us a useful validation rule: if two neighboring terrains are different, there must be an explicit transition defining that relationship. Otherwise, the battlefield is invalid and creation should fail immediately.

## Key Takeaways

- **Autotiling is not only a rendering problem.** Terrain transitions affect battlefield validity and gameplay rules such as occupancy.
- **Resources can become part of the content workflow.** Terrain and transition definitions can be discovered instead of requiring hardcoded identifiers.
- **A transition needs an owner.** Treating `sand_to_void` as automatically bidirectional caused the same physical boundary to be rendered twice.
- **The domain should calculate domain state.** Wang indices describe the result of analyzing neighboring terrain, so the battlefield domain is a better place for that calculation than the view.
- **The presentation layer is simpler when it consumes state.** `BattlefieldView` should render a calculated `TerrainTransition`, not reconstruct one by inspecting its neighbors.

## What's Next

The interesting part now is that we have moved from a visual trick toward a more general terrain system.

The next challenges are no longer just about making the tiles look correct. We can start building on the same transition information for other battlefield behavior, while keeping the content workflow driven primarily by terrain and transition resources.

The goal is for adding a new terrain to increasingly mean **adding content**, rather than changing game code.