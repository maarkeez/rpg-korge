# Plan: faded disabled buttons

> **Status:** done.
>
> **Summary:** A disabled button looked the same as an enabled one: the game's buttons are `DIMGRAY` (`#696969`), and
> KorGE greys a disabled one to `#777777`, keeping its label white. E.g.: with a party of one, the arrows of the
> inventory that switch the party member, and Give reading `No one`, looked as if they could be tapped. Every button
> that can be disabled now fades: a dark background, a grey label and a dim border. See [Buttons](../architecture/buttons.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                 |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | Every button that can be disabled, with one look: the arrows of the inventory and of the shop, Buy or Sell, the item actions (e.g.: `No one`), the steps of the map builder, and Finish turn.          |
| The look     | Faded colors: background `#3A3A3A`, label `#8A8A8A`, border `#555555`. Chosen over the whole button at 40% opacity, compared side by side in the Storybook: the opacity kept the label white, and Finish turn a dim blue that still looked tappable. |
| A button of another color | Fades the same: Finish turn, blue, turns dark grey once disabled.                                                                                                                           |
| Architecture | One `GameButton`, a `UIButton` in `shared.adapters.presentation`, with its `gameButton` builder, holds the colors of the game and the disabled look. The views drop their own copies of the colors. |
| Stories      | **Shared / Game button**: enabled, disabled and in another color. **Screen / Inventory › Exploring, only member**: a party of one, both arrows disabled, with a new `partySize` control.                  |
| Tests        | `GameButtonTest`, through the `isFaded()` page object. The tests of each view that disable a button also check it is faded.                                                                            |
| Out of scope | The buttons drawn with images (the abilities, the bag, the virtual pad, the terrains of the map builder) and the Storybook's own buttons.                                                              |

## As built

- **The hover of the steps of the map builder** was KorGE's blue, as they set no hover color: as game buttons, it is
  the darker grey of the other buttons.
- **The label and the border** fade in `updatedUIButton`, which KorGE calls whenever `enabled` changes, as the
  background only is KorGE's.
- **Checked** with the [`storybook` skill](../../.claude/skills/storybook/SKILL.md): before and after on
  **Exploring, only member**, **Exploring, last member**, **No one to give to**, **Buy, not enough gold**,
  **Smallest map** and Finish turn's **Disabled**.
