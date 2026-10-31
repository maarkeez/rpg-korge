# Plan: The battle HUD drawn as vectors around its pixel art

> **Status:** to do.
>
> **Summary:** The battle HUD mixes three things: the artist's pixel art, KorGE's default widgets, and pixel art drawn
> at scales that break its grid. The mage's portrait sits on a white KorGE button, its venom icon is drawn at 1× beside
> 3× portraits and abilities, and the ability info draws the skull icon and the 8×8 mana diamond and cooldown hourglass
> at 1.5×, so some of their pixels are one screen pixel wide and others two. The selected ability is framed by a pixel
> art frame (`ability_selection.png`), and selecting one docks its [ability info](ability-info.md) in the 110 pixels
> left under the buttons, too few for a blocked ability with two effects. The attack preview shows one receiver at a
> time, a drag away from the next, and never names the ability being cast.
> Instead, pixel art is kept for what the artist draws (portraits, ability and effect icons, the chest), always at a
> whole scale, and the chrome around it is drawn as vectors: rounded panels, bars, pips, pills, rings and glyphs. The
> battle unit info keeps its layout while choosing, and collapses its header to a strip once an ability is selected,
> giving its room to the ability info, drawn as a card. The attack preview becomes a forecast: the ability and its caster
> on one line, then a slim row per battle unit the cast changes, all in sight. Chosen among the Storybook prototypes of
> the branch `prototype/hud-vector-ui`: variant E of the battle unit info, A of the ability info and C of the attack
> preview.

## Decisions

| Topic              | Decision                                                                                                                                                                                                                                                                                                       |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope              | The battle unit info, the ability info and the attack preview, in a battle and while exploring, and the shared views they are built from. E.g.: the formation screen's portraits and bars change too, as they share `UnitPortraitView`, `HealthBarView` and `ManaBarView`.                                       |
| Pixel art          | Kept for the portraits, the ability icons, the effect icons and the chest, only at whole scales (1×, 2×, 3×). Drawn as vectors: the selection frame, the mana and cooldown icons, the cooldown badge, the bars, the points, the panels and the reach diagram. Chosen over redrawing the chrome as pixel art, which would need an asset per size and state. |
| Shared views       | Restyled in place: `UnitPortraitView` draws a dark rounded slot instead of the white KorGE button, and `BarView` and `PreviewBarView` draw rounded bars with their value beside them. Chosen over new views for the HUD only, which would leave the formation with the old style.                                   |
| Battle unit info   | Choosing an ability: today's layout, as a rounded panel 382×120, then the six ability buttons. An ability selected: the header collapses to a strip 382×52 with what a cast spends (the portrait at 1×, health, mana, steps and casts), leaving out the name, the summon's turns left, the effects and the bag, and the ability info takes the room. Chosen over keeping the header (variant A), which leaves the info 110 pixels, and over a compact header at all times (variants B and C), which hides the effects and the bag while choosing. |
| Collapse           | The ability buttons and the info slide up as the header shrinks, in 150ms (`FOCUS_DURATION`), easing out, and back down when the ability is closed. Chosen over an instant swap, where the buttons seem to jump.                                                                                                |
| The bag            | Hidden while an ability is on show, read-only or with its cast range. Tapping the ability again or a tile brings the header and the bag back. `InventoryPresenter` no longer selects the battle unit again before opening the inventory: the bag can not be tapped with an ability on show. This replaces the decision of the [ability info while exploring](ability-info-while-exploring.md), which kept the bag to save a step. |
| Team ring          | The portraits are ringed with their team's colour: green (`#5AD25A`) for the human player's battle units, red (`#FF375F`) for the CPU's, and grey (`#8A8AA0`) for a receiver the cast would defeat. `BattleUnitInfo` gains `isAlly`. Chosen over a neutral slot, as nothing else in the info tells an ally from an enemy. |
| Ability buttons    | Rounded slots 54×54, the icon at 3×. Selected: a gold ring (`#FFD84A`) with a faint glow, replacing `ability_selection.png`. In cooldown: the darkened icon under a dark overlay with the turns left, large, in its middle. Short of mana: the blue icon with a blue cost badge in its corner (e.g.: `● 10`).    |
| Ability info       | A card: the icon at 2× in a slot, the name, the targeting under it, and the costs as pills on the right (mana always, cooldown only when it has one). A blocked ability's reason is a tinted banner under the header. The effects follow, a gold diamond each, their numbers in gold, beside a rounded 3×3 diagram. Chosen over numbers first (B), a tooltip on the button (C) and the diagram first (D). The cooldown of 0 is no longer shown as `-`: its pill is left out. |
| Attack preview     | A forecast: a header with the ability's icon at 2×, its name, `by Mage` and the mana spent as a pill (e.g.: `● -10 mana`), then a row 44 high per battle unit: the portrait at 1×, the name, a KO pill when defeated, the new effects (blinking, as today), the health bar from before to after, a slim mana bar, the change in large (`-12`, `+10`) and `30 → 18`. The caster gets a row only when its health or effects change, e.g.: heal. Chosen over today's rows (A), a versus (B) and cards (D). |
| Receivers' mana    | Kept, as a slim bar 4 high under the health. Chosen over dropping it, though no cast changes it today.                                                                                                                                                                                                         |
| No scroll          | The rows are all in sight, no drag: 5 rows fit in the 300-high HUD, and a cast reaches at most the caster and its 4 adjacent tiles. Chosen over keeping the drag for a future ability reaching more; a new targeting fails `AbilityInfoMapper`, which is where to revisit it.                                     |
| Texts              | The new words are translated: `MOVE`, `CAST`, `HP`, `MP`, `KO`, `by {unit}` and `-{mana} mana`, in `UiText`, `messages.pot` and `es.po`.                                                                                                                                                                        |
| Architecture       | All in `screen.adapters.presentation` and `shared.adapters.presentation`. The palette and the panel are `HudStyle`, plain Kotlin. The one new view is `PillView`. No domain nor use case changes: `BattlefieldPresenter` passes the ability id and `isAlly` it already reads.                                       |
| Storybook          | The stories of every view changed are updated, `PillView` gets its own, and **Screen / Battle unit info** gains **Ability selected, blocked**. The prototypes, never committed to `main`, are left behind with their branch.                                                                                     |
| Tests              | View tests through the views' page objects, and presenter tests. The drag tests of `AttackPreviewViewTest` and the reselect tests of `InventoryPresenterSwitchTest` go.                                                                                                                                         |
| Out of scope       | The effects' turns left and strength, a pixel font, the inventory and shop panels, the virtual pad, the allies a self cast also heals (e.g.: mending's adjacent allies, still left out of its preview), and the health bars over the battlefield tiles (`BattlefieldHealthBarPreviewView`).                         |
| Delivery           | 4 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                  |

For example, the mage, with 18 of its 30 health points, 8 of its 20 mana points, one step and its cast left, selects
bee, in cooldown for 2 more turns:

| Area            | Choosing (y)                                                       | Bee selected (y)                                                            |
|-----------------|--------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Header          | 0–120: portrait 3×, `Mage`, bag, `MOVE ▬▭▭ CAST ▬`, HP, MP, effects | 0–52: portrait 1×, HP `18/30`, MP `8/20`, `MOVE ▬▭▭`, `CAST ▬`              |
| Ability buttons | 128–182                                                            | 60–114, bee ringed in gold                                                  |
| Ability info    | none                                                               | 122–260: bee, `● 10`, banner `Ready in 2 turns`, `Summons Bee for 5 turns (10 HP).` |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone              | Done when                                                                                                                              | Status | Done in |
|----|------------------------|----------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The vector chrome      | The portraits, bars, points, effects and ability buttons are drawn with the new chrome, in the HUD and the formation, and `ability_selection.png` is gone. | ✅      | `f0de11bb` |
| M2 | The ability info card  | Selecting an ability shows its info as a card, with its reason as a banner, and the 8×8 info icons are gone.                           | ✅      | `259b2252` |
| M3 | The focused header     | Selecting an ability collapses the header to a strip, hiding the name, the effects and the bag, and closing it brings them back.       | ✅      | `f0193821` |
| M4 | The forecast           | Previewing a cast shows the ability, its caster and a row per battle unit it changes, all in sight.                                    | ⬜      |         |

### M1 commits

1. Add `HudStyle` and `PillView`, with `PillViewStories` and its tests.
2. Restyle `UnitPortraitView` (the dark slot, the team ring), `BarView`, `PreviewBarView` and `PointsView`, updating
   their stories and tests. `BattleUnitInfo` gains `isAlly`, which `BattleUnitInfoMapper` takes from the presenters.
   `PreviewBarView` is restyled with the forecast rows in M4 instead, as until then its rows are scrolled in a clip
   container, which does not cut the round rects.
3. Restyle `EffectView` as a chip, and `AbilityButtonView` with the ring, the cooldown overlay and the cost badge.
   `BattleUnitInfoView.Ability` gains `manaCost`, taken from `SearchAbilityAvailabilities.AbilityAvailability`, which
   gains it too, as `InventoryPresenter` has no `AbilityApi` to read the cost from.
4. Delete `resources/ability/ability_selection.png` and `art/ability/ability_selection.aseprite`, and its exclusion in
   `StoryContext.abilityIds()`.
5. Update [Assets](../architecture/assets.md) and [Presentation timeline](../architecture/presentation-timeline.md#remaining-turn-actions).
   Until M3 lays out the header anew, the portrait's slot grows to 104 for its margin, and the name and the bars narrow
   to fit.

### M2 commits

1. Add the texts of the new words to `UiText`, `messages.pot` and `es.po`.
2. Redraw `AbilityInfoView` as a card, updating its stories and tests.
3. Delete `resources/ability/info/` and `art/ability/info/`, the 8×8 mana and cooldown icons. Built as one commit with the
   second, as the pre-commit hook stages every tracked change.

### M3 commits

1. Lay out the header of `BattleUnitInfoView` as a panel, with the captions, the summon's turns left as a pill, and the
   bag in a slot.
2. Collapse it on `displayAbilityInfo` and expand it on `hideAbilityInfo` and `display`, with the slide, and the
   **Ability selected, blocked** story. Built as one commit with the first, as both lay out the same view. The team
   ring and the selection ring are drawn as filled shapes under the art, the slot inset over them, as KorGE draws a
   stroke thinner and fainter than its thickness.
3. Drop the reselect from `InventoryPresenter`, with its tests, and update [Inventory](../architecture/inventory.md) and
   [Exploration](../architecture/exploration.md).

### M4 commits

1. Rework `ReceiverPreviewView` as a forecast row, with its stories and tests.
2. Rework `AttackPreviewView` as the forecast, without the drag, taking the ability id, with its stories and tests.
3. Pass the ability id from `BattlefieldPresenter`, and update
   [Presentation timeline](../architecture/presentation-timeline.md#casting-an-ability).

## Reference

### HudStyle

`HudStyle`, in `screen.adapters.presentation`, plain Kotlin: the palette and the panel every HUD view draws with.

| Constant          | Colour                    | For                                                         |
|-------------------|---------------------------|-------------------------------------------------------------|
| `PANEL`           | `#171725`                 | The panels: the header, the ability info, the forecast rows |
| `PANEL_RAISED`    | `#222236`                 | The slots: the portraits, the ability buttons, the bag      |
| `STROKE`          | `#363650`                 | The 1-pixel border of the panels and slots                  |
| `TRACK`           | `#0E0E18`                 | The empty part of the bars                                  |
| `MUTED`           | `#A4A4BE`                 | The secondary texts, e.g.: the targeting, `30 → 18`         |
| `FAINT`           | `#6C6C88`                 | The captions: `MOVE`, `CAST`, `HP`, `MP`                    |
| `SELECTION`       | `#FFD84A`                 | The selected ring, the numbers in the effects, the bullets  |
| `ALLY`, `ENEMY`, `DEFEATED` | `#5AD25A`, `#FF375F`, `#8A8AA0` | The team rings                                 |

- `Container.panel(size, fill = PANEL)` draws a rounded rectangle, radius 10 (8 for a slot), with the `STROKE` border.
- The health red (`RGBA(255, 55, 95)`), the mana blue (`RGBA(0, 145, 255)`) and the cast orange
  (`BattleUnitInfoView.CAST_POINTS_COLOR`) stay as they are.

### PillView

- `PillView(text, color, glyph = Glyph.NONE, solid = false)`, in `screen.adapters.presentation`: a capsule as high as
  its text plus 8, tinted with the colour at 16% and outlined at 45%, the text in the colour. `solid` fills it, the text
  white, e.g.: `KO`.
- `Glyph.MANA` draws an orb (a blue circle with a white highlight) and `Glyph.COOLDOWN` a clock (a ring with two hands)
  before the text, as vectors. They replace the 8×8 `mana.png` and `cooldown.png`.
- Used for the costs of the ability info (`● 10`, `◷ 2`), the summon's `Vanishes in 3 turns`, the forecast's
  `● -10 mana` and `KO`.

### Shared views

- `UnitPortraitView(size, resourcesLoader, portraitScale)`: a `PANEL_RAISED` slot, radius 8, the portrait 4 pixels in.
  `display(unitId, ring = null)` rings it 2 pixels wide in the colour given. The hover colour of the KorGE button goes,
  as the portrait was never tapped.
- `BarView(size, color)`: the bar takes the width left of its value, right aligned, 12 high (e.g.: `18/30`): a `TRACK`
  rounded track and the fill over it, radius half its height. `FormationView` shows the same.
- `PreviewBarView`: the part lost (e.g.: from 30 to 18) is drawn lighter (`#FFC4CE` for the health, `#96CDFF` for the
  mana) and a part gained in green, its value `30 → 18`, the arrow drawn as a vector, as the default font has no `→`.
- `PointsView`: rounded pips 14×7, 3 apart, filled while they remain, and tinted at 14% with a 1.5 outline once spent.
  Replaces the circles.
- `EffectView`: the icon at 1× in a `PANEL_RAISED` chip 20×20, outlined in `SELECTION` when it is new.
- `AbilityButtonView`: the slot, the icon at 3×. `select()` draws the ring: 2 pixels of `SELECTION`, with 3 pixels of it
  at 30% around them. In cooldown, a black overlay at 44% and the turns left, 23 high, centered. Short of mana, a blue
  `PillView` with `Glyph.MANA` and the cost at its bottom right. `display(abilityId, cooldownTurnsLeft, hasEnoughMana,
  manaCost)`.

### Texts

| `UiText`              | Key                      | English          | Spanish            |
|-----------------------|--------------------------|------------------|--------------------|
| `HUD_MOVE`            | `ui.hud.move`            | MOVE             | MOVER              |
| `HUD_CAST`            | `ui.hud.cast`            | CAST             | LANZAR             |
| `HUD_HEALTH`          | `ui.hud.health`          | HP               | PV                 |
| `HUD_MANA`            | `ui.hud.mana`            | MP               | PM                 |
| `ATTACK_PREVIEW_KO`   | `ui.attackPreview.ko`    | KO               | KO                 |
| `ATTACK_PREVIEW_BY`   | `ui.attackPreview.by`    | by {unit}        | de {unit}          |
| `ATTACK_PREVIEW_MANA` | `ui.attackPreview.mana`  | -{mana} mana     | -{mana} de maná    |

### AbilityInfoView

- 380 wide, a `PANEL` card. Top to bottom:
  - The header, 40 high: the icon at 2× in a slot 40×40, the name 16 high, the targeting 12 high in `MUTED` under it,
    and on the right the mana `PillView` and, when the ability has a cooldown, the cooldown one. E.g.: skull, `● 10`;
    mending, `● 10` `◷ 2`.
  - The reason, when blocked: a banner 24 high across the card, tinted in its colour (orange `Ready in 2 turns`, blue
    `Needs 10 mana, has 4`).
  - A 1-pixel `STROKE` rule.
  - The effects, a gold diamond each, wrapped beside the diagram, their numbers in `SELECTION`.
  - The diagram: the rounded 3×3 cells, 10 wide with a 2-pixel gap, at the right of the effects.
- With three effects and a reason it is 170 high, within the 178 the collapsed header leaves.

### BattleUnitInfoView

- **Choosing** (`display(...)`): a `PANEL` 382×120. The portrait at 3× with its team ring, on the left. On its right:
  the name, 18 high, and the summon's turns left as a `PillView`; `MOVE` and the steps, `CAST` and the casts; `HP` and
  `MP` with their bars; the effects. The bag in a slot 40×40 at the top right. Under the panel, the six ability buttons
  54×54, at y 128.
- **Focused** (`displayAbilityInfo(abilityInfo)`): a `PANEL` 382×52 with the portrait at 1× and its ring, `HP` and
  `MP`, and `MOVE` and `CAST` stacked on the right. The name, the turns left, the effects and the bag are hidden. The
  buttons move to y 60 and the info to y 122.
- The move is a slide of `FOCUS_DURATION` (150ms), `EASE_OUT_QUAD`, on the view's `focusAnimator`, cancelled when the
  view is displayed or focused again. `hideAbilityInfo()` and `display(...)` slide back to choosing.
- `displayBag(onBagSelected)` keeps the bag for when the header expands: a focused view shows it once it is back to
  choosing. `hideBag()` hides it either way.

### AttackPreviewView

- `display(abilityId, casterBattleUnit, manaCost, receivers, casterRemainingHealthPoints, casterNewEffectIds)`.
- The header, a `PANEL` 382×52: the ability's icon at 2× in a slot 40×40, its name 15 high, `by Mage` in `MUTED`, and
  `● -10 mana` on the right, left out for a free ability.
- A `ReceiverPreviewView` row per battle unit, from y 60, every 48: the caster first, when the cast changes its health
  or its effects, then the receivers in today's order.
- The `clipContainer`, its drag, `snapToRow` and `scrollAnimator` go.

### ReceiverPreviewView

- A `PANEL` 382×44, radius 8: the portrait at 1× ringed `ALLY` for the caster, `ENEMY` for a receiver, `DEFEATED` when
  the cast leaves it with no health. The name, 13 high, then the `KO` pill and the new effects, blinking. Under the
  name, the health `PreviewBarView` 200×7 and the mana `BarView` 200×4, unchanged for the receivers. On the right, the
  change, 18 high, red (`-12`) or green (`+10`), and `30 → 18` under it.
- The `1/3` counter goes, as every row is in sight.

### BattlefieldPresenter

- Passes `isAlly` (`!isCpuBattleUnit`) to `BattleUnitInfoMapper.battleUnitInfo`, for the info and every row of the
  preview.
- Passes `event.abilityId` of `SelfAbilityCastPreviewed` and `EnemyAbilityCastPreviewed` to `AttackPreviewView`.

### InventoryPresenter

- `isAbilityOnShow` and the reselect of `bagSelected` go: the bag opens the inventory straight away.
- Passes `isAlly = true` to `BattleUnitInfoMapper.battleUnitInfo`, as only the human player's battle units have a bag.

### Storybook

- `PillViewStories` (**Screen / Pill**, `screen-pill`): its `text` (`text`), `glyph` (`enum`: `NONE`, `MANA`,
  `COOLDOWN`) and `solid` (`boolean`) controls. Stories: **Mana cost** (`screen-pill--mana-cost`), **Cooldown**
  (`screen-pill--cooldown`) and **KO** (`screen-pill--ko`).
- `UnitPortraitViewStories` gains its `ring` control (`enum`: `NONE`, `ALLY`, `ENEMY`, `DEFEATED`) and the **Ally** and
  **Enemy** stories.
- `BattleUnitInfoViewStories` gains **Ability selected, blocked** (`screen-battle-unit-info--ability-selected-blocked`,
  the second ability, in cooldown). Its portrait is an ally's, as the controls are full.
- `AttackPreviewViewStories` gains its `ability` control (`pick`, every ability), drops the drag from its description,
  and its **Several enemies** story shows three rows.
- `ReceiverPreviewViewStories` drops its **One of several** story, as there is no counter.
- `StorySmokeTest` lists the new ids and drops `screen-receiver-preview--one-of-several`.

### Docs

- [Assets](../architecture/assets.md): a section on what is pixel art and what the HUD draws as vectors, and the whole
  scales.
- [Presentation timeline](../architecture/presentation-timeline.md): the remaining turn actions as pips with captions,
  and a cast previewed as a forecast, with no scroll.
- [Inventory](../architecture/inventory.md) and [Exploration](../architecture/exploration.md): the bag is hidden while
  an ability is on show.

### Tests

- `PillViewTest`
  - `PillView should display the text in its colour when it is displayed`
  - `PillView should draw the mana orb before the text when its glyph is mana`
  - `PillView should display the text in white when it is solid`
- `UnitPortraitViewTest`
  - `Display should ring the portrait in the colour given when a ring is given`
  - `Display should draw no ring when no ring is given`
- `BarViewTest`
  - `Display should display the value after the bar when it is displayed`
- `PreviewBarViewTest`
  - `Display should display the value before and after the change when it is displayed`
  - `Display should draw the lost part lighter when the value decreases`
  - `Display should draw the gained part green when the value increases`
- `PointsViewTest`
  - `Display should display the spent points as tinted pips when some have been spent` (replaces `Display should display
    hollow points as big as filled points when some have been spent`)
- `AbilityButtonViewTest`
  - `Select should ring the ability when it is selected` (replaces `Select should display ability selection icon when
    is selected`)
  - `Display should display the cost badge when there is not enough mana`
  - `Display should display the cooldown turns left over the ability when it is in cooldown`
- `AbilityInfoViewTest`
  - `AbilityInfoView should display the mana cost and cooldown as pills when it displays an ability`
  - `AbilityInfoView should display no cooldown pill when the ability has no cooldown` (replaces `AbilityInfoView should
    display a dash when the ability has no cooldown`)
  - `AbilityInfoView should display the reason as a banner when the ability is in cooldown`
  - `AbilityInfoView should fit under the focused header when the ability has three effects and a reason` (replaces
    `AbilityInfoView should fit under the ability buttons when the ability has three effects`)
- `BattleUnitInfoViewTest`
  - `BattleUnitInfoView should collapse the header when it displays the ability info`
  - `BattleUnitInfoView should hide the name, the effects and the bag when it displays the ability info`
  - `BattleUnitInfoView should expand the header and show the bag again when it hides the ability info`
  - `BattleUnitInfoView should expand the header when it displays a battle unit while focused`
  - `BattleUnitInfoView should ring the portrait green when the battle unit is an ally`
- `AttackPreviewViewTest`
  - `Display should display the ability and its caster when it is displayed`
  - `Display should display a row for the caster when the cast changes its health`
  - `Display should display no row for the caster when the cast does not change it`
  - `Display should display every receiver in sight when four receivers are provided`
  - The `Drag should ...` tests and `Display should show the caster again when it was scrolled out of view` go.
- `ReceiverPreviewViewTest` (new, it is only tested through `AttackPreviewViewTest` today)
  - `Display should display KO when the receiver would be left with no health`
  - `Display should display the change of health when it is displayed`
- `BattlefieldPresenterTest`
  - `DisplayEnemyAbilityCastPreview should display the ability previewed when an enemy ability cast is previewed`
  - `DisplayMovementRange should ring the portrait red when a CPU battle unit is selected`
- `InventoryPresenterSwitchTest`
  - The `OpenInventory should select the battle unit again before opening the inventory when ...` tests, and `OpenInventory
    should open the inventory straight away once the ability on show is closed`, go.

## Known limitations

- The effects still show no turns left nor strength.
- A self cast's preview still leaves out the allies it heals, e.g.: mending's adjacent allies.
- The slide plays outside the presentation timeline, as a view's own animation: the player may tap during its 150ms.
- The texts keep KorGE's default font, which is not pixel art either.
