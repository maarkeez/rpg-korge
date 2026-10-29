# Plan: Story coverage

> **Status:** in progress: M1, M2, M3 and M4 are done.
>
> **Summary:** Give every view of the game its stories in the [Storybook](../architecture/storybook.md), so the 38
> views [`StoryCoverageArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/StoryCoverageArchitectureTest.kt)
> allows to have none leave its list, one area at a time, and the list is then removed: the rule becomes "every view
> has its stories", with no exception. The views that take domain data are moved to view data first, and
> `BattlefieldView` is split by layer, so every story feeds its view plain data, as the Storybook requires. A new
> architecture test then keeps the views free of the domain.

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
| M5 | Battlefield split         | `BattlefieldView` (1), and the views split from it, which never enter the list                                                                                                                          | `BattlefieldView` is split by layer and takes view data, every view split from it and `BattlefieldView` have stories, and the list is empty. | ⬜      |         |
| M6 | No more allowlist         | None: the list is deleted                                                                                                                                                                               | `viewsWithoutStories` and its test are gone, the guard rule passes, and the docs say every view has its stories, with no exception. | ⬜      |         |

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

The split is grilled again when M5 starts, with `BattlefieldView` as it is then. A starting point:

1. **The battlefield on view data.** What `displayBattlefield` needs of `Battlefield.Dto` (the tiles, their terrain and
   transitions) as view data in `screen.adapters.presentation`, mapped by `BattlefieldPresenter`, and a view
   `Position` for the tiles. No change to what the game shows.
2. **The split.** The terrain tile, the tile overlays and the unit sprite out of `BattlefieldView`, as views of their
   own, with view tests. `BattlefieldView` keeps the tile window, the camera, the trail and the input.
3. **Their stories.** One binding per view split, and `BattlefieldView` with a tiny fixed map (a few tiles of each
   terrain, a battle unit, an NPC, a building and loot), with stories for a move range, a cast range, a selected tile
   and a health preview, logging `tileSelected`.
4. **The plan.** These commits.

### M6 commits

1. **The rule without exceptions.** `viewsWithoutStories` and its test deleted from `StoryCoverageArchitectureTest`;
   a new `ViewArchitectureTest` (or a test in the existing presentation tests), forbidding the views from importing a
   `.domain` or `.usecases` package; the docs updated.
2. **The plan.** These commits, and the status set to done.

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
| `BattlefieldView`, and the views split from it                                           | Screen  | M5        | Split by layer, on view data first.                                   |

### The guard rule

The views are found as `StoryCoverageArchitectureTest` finds them: the top-level classes of an
`adapters.presentation` package extending a KorGE view, directly or through another view of the game, but the
presenters. None of them MUST import a `com.mkz.rpg.<subdomain>.domain` or `.usecases` package. The presenters keep
mapping the domain to the view data. With no view importing the domain by M6, the rule needs no allowlist.
