# Plan: Story coverage

> **Status:** done: M1 to M6 are done. See [Storybook](../architecture/storybook.md#architecture-rules) and [How to add a story](../testing/how-to-add-a-story.md).
>
> **Summary:** Give every view of the game its stories in the [Storybook](../architecture/storybook.md), so the 38
> views [`StoryCoverageArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/StoryCoverageArchitectureTest.kt)
> allows to have none leave its list, one area at a time, and the list is then removed: the rule becomes "every view
> has its stories", with no exception. The views that take domain data are moved to view data first, and
> `BattlefieldView` is split by layer, so every story feeds its view plain data, as the Storybook requires. A new
> architecture test, [`ViewArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewArchitectureTest.kt),
> then keeps the views free of the domain and the use cases.

## Decisions

| Topic               | Decision                                                                                                                                                                                                                                       |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Goal                | Every view of an `adapters.presentation` package has a `<View>Stories` binding, and `viewsWithoutStories` and its test are deleted.                                                                                                             |
| Base views          | `BarView` and `PreviewBarView`, only extended by the bars of the game, get stories too, in the **Shared** group, with a color control. The rule keeps no exception.                                                                             |
| Composite views     | Every view gets its own stories, also when another view contains it (e.g.: `EffectView` and `EffectsView`, `FormationRowView` and `FormationView`, `CancelButton` and `CancelAndConfirmView`). A composite's story builds its real children. |
| Domain data         | The views that take domain Dtos (`BattleUnitInfoView`, `AttackPreviewView`, `ReceiverPreviewView` take `Unit.Dto` and `BattleUnit.Dto`, `NpcDialogView` takes `Npc.Dto`, `BattlefieldView` takes `Battlefield.Dto`) take view data instead: a data class in `screen.adapters.presentation` holding only what the view draws, mapped from the domain by its presenter. The Storybook dependency rule stays as it is. |
| Virtual pad         | `VirtualPadView` gets its own `Direction` enum, which `ExplorationPresenter` maps to `Party.Dto.DirectionDto`, instead of an exception to the dependency rule.                                                                                   |
| Battlefield         | `BattlefieldView` is split by layer before its stories: the terrain tile (with its transitions), the tile overlays (highlight, dim, flash, trail, health preview) and the unit sprite become views of their own, each with stories. `BattlefieldView` stays the composition (the tile window, the camera, the input), with the story of a tiny fixed map. |
| Stories per view    | The states that matter to an artist or a designer, usually 2 to 4: a default, and e.g. empty and full, selected, disabled, a long text. A control for every value the view displays, within `ControlsView.MAXIMUM_HEIGHT`.                     |
| Groups              | The subdomain of the view, as in [How to add a story](../testing/how-to-add-a-story.md): **Screen** for the views of `screen`, **Shared** for `BarView` and `PreviewBarView`.                                                                 |
| Guard rule          | Once no view takes domain data, a new architecture test forbids the views of the game (the classes extending a KorGE view, but the presenters) from importing a `.domain` or `.usecases` package, with no allowlist, so every future view can have stories. |
| End state           | `viewsWithoutStories` and `should only allow the views that have no stories yet...` are deleted. Docs: [Storybook](../architecture/storybook.md#architecture-rules), [How to add a story](../testing/how-to-add-a-story.md) (step 5) and [Package structure](../architecture/package-structure.md) are updated to match. |
| Delivery            | Six milestones, by area, each a few commits straight on `main`, green on CI, the list shrinking each time. See [Milestones](#milestones).                                                                                                       |
| Out of scope        | Visual regression, whole scenes (`BattleScene`, `MapBuilderScene`, `MenuScene`), new controls or globals, and redesigning a view while writing its stories (a design issue found is noted, not fixed, unless it blocks the story).         |

## Milestones

Update the **Status** column as the work progresses: ⬜ to do, 🟡 in progress, ✅ done. Add the commit or pull request
that completes each milestone to **Done in**.

| #  | Milestone                 | Views leaving the list                                                                                                                                                                                  | Done when                                                                                                                         | Status | Done in |
|----|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Bars, icons and badges    | `BarView`, `PreviewBarView`, `ManaBarView`, `HealthBarPreviewView`, `ManaBarPreviewView`, `BattlefieldHealthBarPreviewView`, `PointsView`, `UnitNameView`, `UnitPortraitView`, `RemainingTurnsView`, `EffectView`, `EffectsView`, `AbilityButtonView`, `BagButtonView` (14) | The 14 views have stories, the list holds 24 views, and `jvmTest`, `jsTest` and `lintKotlin` pass.                                 | ✅      | c4b8d2d, 12c8421, feea986 |
| M2 | Buttons, dialogs and menus | `CancelButton`, `ConfirmButton`, `CancelAndConfirmView`, `FinishTurnView`, `GiveItemView`, `ItemActionsView`, `InventoryFullView`, `InventoryView`, `FormationRowView`, `FormationView`, `MenuView`, `BattleInfoView`, `PlayerCallToActionView` (13) | The 13 views have stories, with their callbacks in the Actions, and the list holds 11 views.                                      | ✅      | 56aeba0, 343b4c8, 9d48c7d, 9c5bd59 |
| M3 | Map builder               | `MapBuilderCallToActionView`, `MapBuilderHudView`, `MapBuilderInfoView`, `MapBuilderMenuView` (4)                                                                                                      | The 4 views have stories, and the list holds 7 views.                                                                             | ✅      | 2ad229d |
| M4 | Battle HUD on view data   | `BattleUnitInfoView`, `AttackPreviewView`, `ReceiverPreviewView`, `BattleHudView`, `NpcDialogView`, `VirtualPadView` (6)                                                                               | The 5 views take view data instead of domain Dtos, the 6 views have stories, the game plays as before, and the list holds `BattlefieldView` only. | ✅      | 89f3c61, 32dd3ae, fec7a21 |
| M5 | Battlefield split         | `BattlefieldView` (1), and the views split from it, which never enter the list                                                                                                                          | `BattlefieldView` is split by layer and takes view data, every view split from it and `BattlefieldView` have stories, and the list is empty. | ✅      | 9a24220, b9c3f9c, 1370cc7 |
| M6 | No more allowlist         | None: the list is deleted                                                                                                                                                                               | `viewsWithoutStories` and its test are gone, the guard rule passes, and the docs say every view has its stories, with no exception. | ✅      | 5d1c8e1 |

Each milestone also adds its component and story ids to `StorySmokeTest`, registers its bindings in
`StorybookApi.STORY_BINDINGS` (grouped as the sidebar should list them), regenerates the
[dependency report](../architecture/dependencies.md) when the Storybook depends on a new subdomain, and looks at each
new story with the [`storybook` skill](../../.claude/skills/storybook/SKILL.md).

### M1 commits

1. **The bars.** `BarView` and `PreviewBarView` in **Shared**, with a color control, and `ManaBarView`,
   `HealthBarPreviewView`, `ManaBarPreviewView` and `BattlefieldHealthBarPreviewView`.
2. **The unit badges.** `PointsView`, `UnitNameView`, `UnitPortraitView` (a pick of the unit ids), `RemainingTurnsView`.
3. **The effects and the buttons.** `EffectView` (a pick of the effect ids), `EffectsView`, `AbilityButtonView` (a pick
   of the ability ids, and its cooldown badge), `BagButtonView`.
4. **The plan.** These commits.

### M1 as built

The three commits are as listed, plus these decisions:

- **The sidebar overflows**: with the 79 ids of M1 (17 components, 62 stories), the list is taller than the sidebar, 900
  high, which has no scroll: it is cut after `Battlefield health bar preview`, the seventh component, and the stories
  below are only reachable by a link. It was found by looking at the stories, not fixed (see Out of scope): the sidebar
  needs a scroll, or components that collapse, **before M2** adds a dozen components more.
- **`PreviewBarView` has two color controls**, `filledColor` and `previewColor`, the second drawn at half its alpha, as the
  game does. The preview colors of `HealthBarPreviewView` and `ManaBarPreviewView` are `RGBA.float(205.0, 105.0, 145.0, 0.5)`
  and `RGBA.float(50.0, 95.0, 205.0, 0.5)`, whose channels above 1 clamp: both are white at half alpha. Noted, not
  changed.
- **`StoryContext`** lists the ids of the resources the views take, never written by hand: `unitIds()` (the units
  with a portrait), `effectIds()` and `abilityIds()` (without the selection frame), next to `lootableItemIds()`. Only
  two effects have an icon, `venom-damage` and `venom-on-death`.
- **`BagButtonView` has no control**: it displays nothing but its chest, so it has a single story, `Chest`, logging
  `onBagSelected`. `AbilityButtonView` logs `abilitySelected`, which sets its `selected` arg.
- **`EffectsView`** takes a count of effects and of new effects, which cycle through the effects with an icon.
- **`StorybookEndToEndTest`** opens `screen-health-bar--full` explicitly, and taps its story by id (`clickStoryId`):
  it relied on the health bar being the first story, and on `Empty` being unique.

### M2 commits

1. **The sidebar scrolls.** `SidebarView` lists its components in a clipped list under its title, which scrolls with the
   mouse wheel and by dragging, and scrolls to the story on show when it is out of sight, with view tests; the
   [Storybook](../architecture/storybook.md#what-it-shows) doc says so. Found in M1, needed before M2 adds a dozen
   components more.
2. **The buttons.** `CancelButton`, `ConfirmButton`, `CancelAndConfirmView`, `FinishTurnView`, logging their callbacks.
3. **The item dialogs.** `GiveItemView`, `ItemActionsView`, `InventoryFullView` (an item pick, its price, long names),
   and `InventoryView`.
4. **The formation, the menu and the battle info.** `FormationRowView`, `FormationView`, `MenuView`, `BattleInfoView`,
   `PlayerCallToActionView`.
5. **The plan.** These commits.

### M2 as built

The four commits are as listed, plus these decisions:

- **The sidebar scrolls**: the M1 overflow is fixed first. `SidebarView` lists the components in a clipped `list` under
  its fixed title, which moves with the mouse wheel and by dragging (the drag is relative to where it started, as
  `BattlefieldView`'s), never past either end. Showing a story that is not fully in sight, e.g.: opened from a link,
  scrolls it to the middle of the list; it only does so when the story changes, so an arg change does not undo a scroll
  of the person. The title is drawn over the list, with a background of its own: the rounded rectangle behind each story
  is not clipped by the list, and showed over the title when scrolled. With the 144 ids of M1 and M2 (30 components,
  114 stories) the list is about five times the height of the sidebar.
- **The labels are texts**: `CancelButton`, `ConfirmButton` and `CancelAndConfirmView` take their labels, and the width
  of the buttons, as controls, with English defaults and a long label story: the view takes any text. The views that
  read a `UiText` (`FinishTurnView`, `MenuView`, `BattleInfoView`) follow the language global.
- **Item names are texts**: `GiveItemView`, `ItemActionsView` and `InventoryFullView` take the name of the item as a
  text control, so there is a long name story. Only `InventoryFullView` picks an item (`foundItem`), for its icon.
  `ItemActionsView` has an `actions` enum of the kinds of item the inventory presents: a merchant's item with no
  actions, a consumable (also one that can not be consumed), an equippable, an equipped one, and Give refused for no
  one to give to or full bags.
- **Selection is an arg**: `InventoryView`, `FormationView` and `FormationRowView` log the tap and set `selectedSlot`,
  `selectedRow` and `selected`, clearing it when the selected one is tapped again, as `ShopViewStories` does. The
  arrows of `InventoryView` set `partyIndex`; `InventoryView` and `FormationView` cycle through the lootable items and
  the units with a portrait.
- **`PlayerCallToActionView`** builds its real children, one at a time by a `callToAction` enum. Choosing something
  (Cancel, Confirm, an item action, Leave it) logs it and sets `callToAction` back to `FINISH_TURN`, as the view
  dismisses itself in the game.
- **`BattleInfoView`** shows exploring, the turn of the human or the CPU player in a round, and the winner. The
  announcements that pop (`announceBattle`, `announceBattleEnd`, `announceTurn`) are suspended animations that the
  stories cannot await, so the state they leave is shown, and `Battle!` and `Battle over!` have no story.
- **`MenuView` changes the shared texts**: picking a language in it calls `Texts.selectLanguage`, as in the game, so the
  views on show change language until the next story, arg or global change restores the language global. Playing or
  building a map hides its options, also as in the game, until the story is shown again. It has no controls, and a
  single story.
- **`GiveItemView`, `ItemActionsView` and `InventoryFullView`** are 390 wide like the screen; `FormationRowView` is shown
  at 370x44, the size the formation gives it.

### M3 commits

1. **The map builder views.** `MapBuilderCallToActionView`, `MapBuilderHudView`, `MapBuilderInfoView`,
   `MapBuilderMenuView`.
2. **The plan.** These commits.

### M3 as built

The two commits are as listed, plus these decisions:

- **`StoryContext.terrainIds()`** lists the terrains that have a tile, from the names of the transition strips
  (`sand_to_grass` gives `grass` and `sand`), next to the other ids. `MapBuilderHudViewStories` takes each tile from a
  `BattlefieldView`'s `terrainTileBitmap`, as `MapBuilderPresenter` does: **M5**, which splits `BattlefieldView`, has to
  keep that binding working.
- **The map sizes are written in the bindings** (1 to 128, 16 by default): they are constants of the `mapBuilder` domain,
  which the Storybook must not import. `MapBuilderHudView` and `MapBuilderInfoView` have a `rows` and a `columns`
  control; the steps of the HUD set whichever changed, and tapping a terrain sets `selectedTerrain` (a pick of the terrain
  ids).
- **`MapBuilderInfoView` messages are transient**: an error or an info pops in the subtitle for two seconds and the size
  comes back, as in the game. The `messageKind` (`NONE`, `ERROR`, `INFO`) and `message` controls, and every change of an
  arg, show it again. It has no callbacks. The stories use English messages, not the translations of the presenter.
- **`MapBuilderCallToActionView`** has a `callToAction` enum, and the callbacks do as the map builder: `onBack` sets
  `LEAVE_CONFIRMATION`, `onStayed` brings back `EXPORT_AND_BACK`, and `onExported` and `onLeft` only log themselves (the
  game copies the map, or changes scene).
- **`MapBuilderMenuView`** has an `open` boolean and an `options` count (1 to 6): the first option is the Import of the
  game, the others (`Option 2`, ...) only show a longer list. The open stories open it on the phone, in the first frame,
  as `open()` measures where the view is. It is wrapped in a view as big as its button: the options would otherwise move
  the button when the phone centers it. Choosing an option logs `onSelected` and sets `open` to `false`. In the phone it
  is in the middle, not at the top right: the transparent view catching the taps out of the open menu is placed by the
  position on the whole page, so out of the phone it closes it only partly.
- **The sidebar** lists them after the menu, in the order of the map builder screen: the info, the HUD, the call to
  action and the menu. With them the Storybook has 162 ids (34 components, 128 stories).
- **The HUD** is 390 wide, the width of the phone, so the border of the selected terrain, drawn 3px out of its button,
  is cropped when the first terrain is selected. Noted, not changed.

### M4 commits

1. **The battle unit on view data.** A `BattleUnitInfo` (and what `AttackPreviewView` and `ReceiverPreviewView` need,
   reusing it where it fits) in `screen.adapters.presentation`, mapped from `Unit.Dto` and `BattleUnit.Dto` by the
   presenters displaying them (`BattlefieldPresenter`, `InventoryPresenter`). With the presenter and view tests updated, and no change to what the game shows.
2. **The NPC dialog and the pad on view data.** An `NpcDialog` for `NpcDialogView`, and a `Direction` enum for
   `VirtualPadView`, mapped by `ExplorationPresenter`.
3. **Their stories.** `BattleUnitInfoView`, `AttackPreviewView`, `ReceiverPreviewView`, `BattleHudView` (building its
   real children), `NpcDialogView` and `VirtualPadView`, logging the directions pressed and held.
4. **The plan.** These commits.

### M4 as built

The four commits are as listed, plus these decisions:

- **`BattleUnitInfo`** (`unitId`, health and mana points with their maximums, and `effectIds`: the ones applied when each
  turn starts, followed by the ones applied when it is defeated) is what the three battle unit views draw of a battle unit.
  The name of the unit is read from its `unitId`, so the views no longer take a `Unit.Dto` beside the `BattleUnit.Dto`,
  and `BattlefieldPresenter` and `InventoryPresenter` no longer need the `UnitApi` (nor does `BattleScene` pass it).
  `AttackPreviewView.Receiver` holds a `BattleUnitInfo`, the health it would have left and the new effects.
- **`BattleUnitInfoView` takes what the HUD shows of the turn**: `TurnActions` (steps and casts, left and maximum),
  `remainingTurns` (`null` but for a summon) and a list of `Ability` (the id, the cooldown turns left and whether the
  battle unit has the mana), all nested in the view. `BattleUnitInfoMapper`, internal to `screen.adapters.presentation`,
  maps them from `BattleUnit.Dto` and `AbilityAvailability` for both presenters. The tests have a `BattleUnitInfoMother`.
- **`NpcDialog`** (`npcId`, `unitId`, `name`, `line`): the name and the line are translated by the presenter, so
  `NpcDialogView` no longer takes the `Texts`. The portrait is still the one of the unit when there is one (a recruitable
  npc), or else the npc's own, from the resources.
- **`VirtualPadView.Direction`** (`NORTH`, `EAST`, `SOUTH`, `WEST`): the pad calls its callbacks with it, and
  `ExplorationPresenter` keeps its pending press with it and maps it to `Party.Dto.DirectionDto` when it walks the party.
  The pad is no exception of the dependency rule.
- **`StoryContext.npcIds()`** lists the npcs that have a portrait, next to `unitIds()`, for the pick of the NPC dialog.
- **`BattleUnitInfoViewStories`, `AttackPreviewViewStories` and `ReceiverPreviewViewStories`** feed the views a fixed
  battle unit (30 health points, 20 mana points, 3 steps and 1 cast), and the controls change what the stories show: the
  health, mana, steps, casts, remaining turns, number of effects and of abilities, and for the cast the receivers, the
  mana cost and the health after it. The values are written in the bindings, as the Storybook can not import the
  domain: their maximums are the ones of the stories, not the game's. The effects and abilities go round the ones with an
  icon (`effectIds()` and `abilityIds()`); the abilities alternate ready, in cooldown and without mana. `ReceiverPreviewView`
  has a `position` and a `count`, the first never past the second, as the counter shows only with several receivers.
- **`BattleHudViewStories`** builds its real children with a `display` enum (`BATTLE_UNIT_INFO`, `ATTACK_PREVIEW`,
  `INVENTORY`, `HIDDEN`): the bag logs `onBagSelected` and shows the inventory, closing it logs `inventoryClosed` and
  shows the info, as in the game. The abilities, slots and owner arrows only log themselves.
- **`NpcDialogViewStories`** has the name, the line, an `npc` pick, a `unit` pick (each with a `none` option) and a
  `merchant` flag. The merchant stories build Buy, Sell and Leave from the translated `UiText`, logging `onSelected`
  with the label, and tapping the dialog logs `onTapped`. The name and the line are plain texts, not the translations
  of the npc.
- **`VirtualPadViewStories`** has no control: it logs `onDirectionPressed`, `onAPressed` and `onBPressed`, and
  `onDirectionHeld` only the first frame it is held, as the game calls it on every frame and would fill the Actions.
- **The sidebar** lists them after the player call to action, in the order of the battle screen: the battle unit info, the
  attack preview, its receiver, the HUD, the NPC dialog and the pad. With them the Storybook has 194 ids (40 components,
  154 stories).
- **`dependencies.html`** was regenerated by each commit, as the imports of the screen and of the Storybook grew: the
  Storybook depends on no new subdomain, so `dependencies.md` does not change.
- **Design issues noted, not fixed**: the attack preview draws a thin line across the bars of some rows (the receiver's
  in the stories, the caster's in the HUD one), which the receiver preview alone does not: probably the software
  renderer of the headless browser drawing the clipped viewport, to be checked on a GPU. The attack preview stories
  show the receiver with 10 of 10 mana points, while the receiver preview shows 10 of 20: the stories should agree.

### M5 commits

The split was grilled again when M5 started, with `BattlefieldView` as it was then. As built:

1. **The battlefield on view data.** `BattlefieldTerrain` (the size and the terrain, with its transition, of each tile),
   `TerrainTransition` and `TilePosition` in `screen.adapters.presentation`, mapped from the domain by
   `BattlefieldMapper`, which the battlefield, shop, formation, inventory and map builder presenters use. With the
   presenter and view tests updated, and no change to what the game shows.
2. **The split, with its stories.** `TerrainTileView` (with `TerrainBitmaps`), `TileHighlightView`, `TileDimView`,
   `TileFlashView` and `UnitSpriteView`, each with view tests and its stories. Their stories are in this commit, and not
   in the next one as planned, as the coverage rule would fail without them, and the split views never enter the list of
   views allowed to have none.
3. **The battlefield stories.** `BattlefieldView` with a tiny fixed map, the list of views allowed to have no stories
   emptied (not deleted: that is M6).
4. **The plan.** These commits.

### M5 as built

The four commits are as listed, plus these decisions:

- **`BattlefieldView` takes no domain type**: `displayBattlefield` and `updateTerrain` take a `BattlefieldTerrain`
  (`rows`, `columns`, and a `Tile` with the `terrainId` and a `TerrainTransition?` for each `TilePosition`), and
  `dimTilesExcept` and `moveBattleUnit` take `TilePosition`s. `BattlefieldMapper` (internal, as `BattleUnitInfoMapper`)
  maps the battlefield and the positions; `ShopPresenter`, `FormationPresenter`, `InventoryPresenter` and
  `MapBuilderPresenter` map the positions they dim or update, and `BattlefieldPresenter` the path of a move. The domain
  keeps its `PositionDto`s where the presenters talk to it (e.g.: the cast group, the loot displayed). The tests have a
  `BattlefieldTerrainMother` (`Battlefield.toTerrain()`) and the fake view records `TilePosition`s.
- **The split follows the layers of a tile**: the tile is still a `UIButton` holding named layers (`TERRAIN`, `BUILDING`,
  `LOOT`, `BATTLE_UNIT`, `NPC`, `SELECTION`, `FLASH`, `DIM`, `HEALTH_PREVIEW`...), in the order they are added, so the
  layer names, the order, and what the game shows are the same. Each layer that has a state of its own is a view:
  - **`TerrainTileView`** draws the terrain, or its transition, at a whole scale to fill the tile, with `display(terrainId,
    transition)`, which `updateTerrain` calls again when a tile is painted. The art of the terrain is in
    **`TerrainBitmaps`** (not a view), which splits each strip of transitions into its tiles the first time it is asked, and
    which every tile of the battlefield shares: a view per tile can not each split the strips. `BattlefieldView.terrainTileBitmap`
    stays, for `MapBuilderPresenter`, and reads from the same `TerrainBitmaps`.
  - **`TileHighlightView`** is the frame of a tile, of a `Kind`: `MOVE_RANGE`, `CAST_RANGE` or `SELECTED`.
  - **`TileDimView`** is the black mask, at `DIM_ALPHA`.
  - **`TileFlashView`** is the white silhouette of a battle unit, built by the view from its image. It is a
    **`UnitSpriteView`**, the image of a battle unit or an npc filling its tile, which also draws the battle units gliding
    between tiles. `BattlefieldView` draws and removes the flash to make it blink, as before: the timing stays in the
    composition.
  - The health preview was already a view (`BattlefieldHealthBarPreviewView`), with its stories.
  - These views are `Container`s holding an image (`Image` and `SolidRect` can not be extended), so a layer is no longer
    an `Image` itself: the page object of `BattlefieldView` reads the image of a layer through the view.
- **What stays in `BattlefieldView`**: the tile window, the camera (scrolling, following and trailing), the input, the
  battle units gliding and swapping, the flash blinks, the tint, and the loot and building pieces, which are bare images
  with no state of their own (the loot at its own scale, a building cut into the pieces of its tiles). The picked up item
  rising from its tile is an animation of a bare image too. They are shown in the stories of `BattlefieldView`.
- **The gliding battle unit changed how it is placed**: its image was anchored on its center, in a container, and is now a
  `UnitSpriteView` at the corner of the container. It draws the same pixels, and the swap still hops it by moving it; the
  page object of the gliding units follows.
- **The flash no longer caches its silhouette per unit**: each `TileFlashView` builds it, a 16x16 or 32x32 image, when it
  blinks (three times per flash).
- **`StoryTiles`** (internal to the Storybook, not named `...Stories`, as the coverage rule would take it for a binding)
  builds a terrain tile with the layers over it, as the stories of the layers draw them: a highlight, dim or flash over
  the terrain tile, which a `terrain` pick changes, and a unit sprite under them. **`StoryContext.terrainTransitionIds()`**
  lists the strips of transitions, e.g.: `sand_to_grass`, for the `transition` pick of the terrain tile.
- **`MapBuilderHudViewStories`** still takes each tile from a `BattlefieldView`'s `terrainTileBitmap`, as the presenter
  does: it is unchanged, as `terrainTileBitmap` stayed on `BattlefieldView`.
- **The stories of the split views**: `TerrainTileViewStories` (sand, water, and the transitions sand to grass and sand
  to void, with a `wangIndex` from 0 to 15), `TileHighlightViewStories` (the three kinds, over sand and over water),
  `TileDimViewStories` (over a terrain, a battle unit and lava), `TileFlashViewStories` (the two frames of the blink, with
  a `flashing` flag, and a small unit) and `UnitSpriteViewStories` (a 32x32 and a 16x16 unit, an npc, on lava). They are
  64 pixels, the real size of a tile, so small in the 390x844 phone.
- **`BattlefieldViewStories`** feeds the view a 6x6 map, as big as its viewport, so it does not scroll: sand with a patch
  of grass, of water, of lava and of void, a fortress (4x4 tiles), a battle unit (`unit` pick) and another one, an npc
  (`npc` pick, with `none`), and loot (`loot` pick): an item and a chest. The transitions of the tiles are worked out
  by the binding from the strips of the resources, the way the battlefield domain does (north 1, east 2, south 4, west
  8 for the sides touching the target terrain), as the Storybook can not import it. A `layer` enum draws over the tiles
  what the battle draws for the selected battle unit: the move range (the unit's tile selected, the tiles it can
  move to framed, the others dimmed), the cast range, or a cast previewed (a `healthPreview` pick on the battle unit
  of the cast group, the vacant tiles selected). The tiles of each range are written in the binding, as the domain
  works them out. `selectedTile` is a text, `row,column` or `none`, which tapping a tile sets, logging
  `tileSelected(row, column)`; anything else, or a tile out of the map, selects none. Stories: Map, Move range, Cast
  range, Selected tile, Health preview and Health preview, defeated.
- **The sidebar** lists the battlefield first, then its layers (the terrain tile, the highlight, the dim, the flash and the
  unit sprite) and the health bar preview, which moved from the bars of M1 to be with them, and then the battle unit info.
  With them the Storybook has 224 ids (46 components, 178 stories).
- **`dependencies.html`** was regenerated by each commit, as the imports of the screen and of the Storybook grew:
  `dependencies.md` does not change.
- **`viewsWithoutStories`** is `emptySet<String>()`, with its test, until M6. `BattlefieldView` imports no `.domain` or
  `.usecases` package any more.
- **Design issues noted, not fixed**: the loot, the building pieces and the picked up item are bare images in
  `BattlefieldView`, not views of their own: a view for them is for when they get a state to show. `TileFlashView` and the
  tile dim could be one view each with their timing (`BattlefieldView` still blinks the flash with a coroutine per tile).
  The `layer` control wraps to two rows of the Controls. The rat, drawn in black, hardly stands out over the void of the
  map; the transitions of the map follow the art of the strips.

### M6 commits

1. **The rule without exceptions.** `viewsWithoutStories` and its test deleted from `StoryCoverageArchitectureTest`;
   a new `ViewArchitectureTest` (or a test in the existing presentation tests), forbidding the views from importing a
   `.domain` or `.usecases` package; the docs updated.
2. **The plan.** These commits, and the status set to done.

### M6 as built

The two commits are as listed, plus these decisions:

- **`ViewArchitectureTest`** is a new test class next to the other architecture tests, with Konsist as them, and one
  rule: no view of the game imports a name matching `com.mkz.rpg.<subdomain>.domain.*` or `.usecases.*` (the imports of
  the file the view is declared in). It has no allowlist, and fails naming each import and its view.
- **`GameViews`** (internal, in the architecture tests) holds how the views are found, moved out of
  `StoryCoverageArchitectureTest` so both tests share it instead of copying it: the top-level classes of an
  `adapters.presentation` package, but the presenters and the Storybook's own views, extending a KorGE view directly or
  through another class of the project. `StoryCoverageArchitectureTest` keeps the story bindings and the registry rule.
- **`viewsWithoutStories`** and `should only allow the views that have no stories yet...` are deleted, with the
  `- viewsWithoutStories` of the coverage test: the rule is "every view has its stories". The Storybook, package structure
  and how-to docs say so, with the new rule, and step 5 of the how-to no longer asks to take the view out of a list.
  [Plan: Storybook](storybook.md), done, keeps its allowlist as built, with a pointer here.
- **The guard found one violation**: `MapBuilderHudView` imported `MapBuilder` for `DEFAULT_SIZE`, `MINIMUM_SIZE` and
  `MAXIMUM_SIZE`, which M3 to M5 missed because only the Storybook's imports and the views taking domain Dtos were
  looked at. It is fixed as M4 and M5 did: `displaySize` takes the `allowedSizes` (an `IntRange`), which
  `MapBuilderPresenter` passes from the domain's sizes, and the view starts with no size, drawing nothing until the
  presenter displays one (it showed `16` before the presenter did). The view test has a case with other sizes (4 to 20),
  and the presenter test checks the sizes it passes. The story still writes its own 1 to 128 (M3 decision).
- **Verified by breaking it**: a `com.mkz.rpg.battle.domain.Battle` import added to `ManaBarView`, which extends
  `BarView`, makes the test fail with `com.mkz.rpg.battle.domain.Battle (ManaBarView)`, and it is reverted. The test also
  failed on the real `MapBuilderHudView` import above before its fix.
- **`dependencies.html`** was regenerated: one import of `mapBuilder.domain` fewer from the screen (the view's), the presenter keeps its own.
- **Not changed**: `StorySmokeTest`, the bindings and the story ids. The scenes (`BattleScene`, `MapBuilderScene`,
  `MenuScene`) and the presenters are not views, and keep importing the domain.

## Reference

The names are proposals: keep them unless an architecture test or a better name found while building asks otherwise,
and update this section when they change.

### The views and their area

| View                                                                                     | Group   | Milestone | Notes                                                                 |
|------------------------------------------------------------------------------------------|---------|-----------|-----------------------------------------------------------------------|
| `BarView`, `PreviewBarView`                                                              | Shared  | M1        | Base views, in `shared.adapters.presentation`. A color control.       |
| `ManaBarView`, `HealthBarPreviewView`, `ManaBarPreviewView`, `BattlefieldHealthBarPreviewView` | Screen | M1   | As `HealthBarViewStories`.                                            |
| `PointsView`, `UnitNameView`, `UnitPortraitView`, `RemainingTurnsView`                   | Screen  | M1        | `UnitPortraitView` needs the resources, from `StoryContext`.           |
| `EffectView`, `EffectsView`, `AbilityButtonView`, `BagButtonView`                        | Screen  | M1        | The ids from what the resources loaded, never written by hand.        |
| `CancelButton`, `ConfirmButton`, `CancelAndConfirmView`, `FinishTurnView`                | Screen  | M2        | Lambdas, logged as actions.                                           |
| `GiveItemView`, `ItemActionsView`, `InventoryFullView`, `InventoryView`                  | Screen  | M2        | `context.lootableItemIds()` for the items.                            |
| `FormationRowView`, `FormationView`, `MenuView`, `BattleInfoView`, `PlayerCallToActionView` | Screen | M2       |                                                                       |
| `MapBuilderCallToActionView`, `MapBuilderHudView`, `MapBuilderInfoView`, `MapBuilderMenuView` | Screen | M3     |                                                                       |
| `BattleUnitInfoView`, `AttackPreviewView`, `ReceiverPreviewView`, `BattleHudView`        | Screen  | M4        | On view data first.                                                   |
| `NpcDialogView`, `VirtualPadView`                                                        | Screen  | M4        | On view data first; the pad's own `Direction`.                        |
| `BattlefieldView`                                                                        | Screen  | M5        | On view data first (`BattlefieldTerrain`, `TilePosition`); a tiny map. |
| `TerrainTileView`, `TileHighlightView`, `TileDimView`, `TileFlashView`, `UnitSpriteView` | Screen  | M5        | Split from `BattlefieldView` by layer; `TerrainBitmaps` is not a view. |

### The guard rule

The views are found as `StoryCoverageArchitectureTest` finds them: the top-level classes of an
`adapters.presentation` package extending a KorGE view, directly or through another view of the game, but the
presenters. None of them MUST import a `com.mkz.rpg.<subdomain>.domain` or `.usecases` package. The presenters keep
mapping the domain to the view data. With no view importing the domain from M6 on, the rule has no allowlist.
