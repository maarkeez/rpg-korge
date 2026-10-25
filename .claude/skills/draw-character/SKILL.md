---
name: draw-character
description: Turns a vague idea for a humanoid character (a new one, or a redraw of one in the cast) into a 32x32 sprite and portrait in the game's A Link to the Past style. Interviews the user, proposes a design, has a subagent draw it with the humanoid brief, reviews it on a comparison page, and on approval puts it in the game and pushes it.
argument-hint: "<idea, e.g. 'a grumpy dwarf blacksmith' or 'redraw the knight'> [model or effort override]"
disable-model-invocation: true
---

# Draw a character

The user comes with an idea, often vague ("a shady merchant", "redraw the knight"). You are the pixel art expert: you
know the [humanoid brief](../../../docs/design/humanoid-characters.md), the [`pixel-art` skill](../pixel-art/SKILL.md)
and its `tools/pixel-art/pixel` tool, and what A Link to the Past does. Turn the idea into a design, get it drawn, show
it, and stop at each of the user's decisions.

Humanoids only: humans, elves, orcs and the like. For a creature (a bat, a goat, a slime), say there is no brief for it
yet and stop.

## 1. Learn the style

Before talking design, read the whole humanoid brief and the `pixel-art` skill, and look at the reference and at the
characters already drawn with this version (the "Drawn now" column of "Redrawing the cast"): `pixel preview` them and
read the PNGs. Keep their files in mind: `art-drafts/reference/link-front` and `link-side`, `art/npc/elf`,
`art/unit/hero`.

## 2. Interview

Ask 3-5 targeted questions in one round (with the AskUserQuestion tool when it is available), only about what the idea
leaves open and the [Cast](../../../docs/design/humanoid-characters.md#cast) table does not already say. Offer concrete
options that fit the brief, the recommended one first. Pick from:

- **Role:** a playable unit (its abilities) or an npc (where it stands, what it says), and its id.
- **Species and build:** human, elf, orc, or a new species (then the brief's Species table needs a row); a child, an
  adult, a commander, a brute.
- **Mood and expression:** what the face says (watchful, smug, stern, tired, cheerful).
- **Look:** hair or headgear, clothes, and their ramps, from the brief's Colours tables. Name the colours, not just
  "blue".
- **Gear:** what it holds and on which side, within the brief's Gear rules.
- **Facing:** front, or three-quarter right.

For a redraw, the character's Cast row is the design: ask only what the user wants changed, and look at its current
sprite and portrait (`pixel preview`) for its design.

## 3. Design card

Propose the design in a few lines, then the Cast row it will get (the brief's columns: id, role, LttP reference, look,
gear), and say how it stands apart from the rest of the cast in silhouette and colour. Wait for the user's approval or
changes. Do not draw before it is approved.

## 4. Draw, with a subagent

Delegate the drawing to a fresh subagent with the Agent tool: model `opus`, effort `high`, unless the user asked for
another model or effort. Run it in the foreground, and keep its agent id for later rounds. Its prompt must say:

1. Load the `pixel-art` skill and follow it.
2. Read `docs/design/humanoid-characters.md` in full and follow it exactly: the reference, the template, Camera,
   Silhouette and pose, Proportions, Face, Species, Lines and light, Colours, Gear, Portraits, Layers, Review and
   Checklist.
3. The approved design card, pasted in full.
4. Draw the sprite at `art-drafts/humanoid/<id>.aseprite` and the portrait at `art-drafts/humanoid/<id>_portrait.aseprite`,
   both 32x32, each PNG exported next to its source. Start from scratch: never from the character's current files nor
   any other character's; the elf and the hero only to match scale and finish.
5. Do the brief's Review step for real, beside the reference at the same scale and at 1x, and iterate. Then the
   Checklist, with a script for the outline rule (every silhouette pixel, diagonals and image edges included, with a
   luminance under 60 must be `W`).
6. Do not touch `art/`, `resources/` or the brief, and do not commit.
7. Report: the two PNG paths, the final `pixel grid` of both, their colour counts, what the review found and how it was
   fixed or why it was kept, and anything in the brief that was unclear, contradictory or hard to follow.

Pass on any lesson from earlier characters that the brief does not hold yet (each Cast row's "To improve on a redraw").

## 5. Review and show

Before the draft goes in front of the user, save the images it is compared with: for a redraw, copy the current
`resources/.../<id>.png` and `<id>_portrait.png` to the scratchpad, since installing overwrites them.

Look at the drafts yourself, at 6-8x and at 1x, next to `link-side` (or `link-front`) and the elf and the hero. Judge
them against the brief's Review: camera, face, light, lines, gear, and whether the portrait matches the sprite.

Publish one comparison page with the Artifact tool (load its design skill first):

- The sprite strip: before (for a redraw), each draft so far, the reference, then all of them at 1x.
- The portrait strip: before, each draft so far.
- Under each strip, "Better" and "Still off", a few concrete lines each, honest about what failed.
- Embed the PNGs as `data:` URIs, scaled with `image-rendering: pixelated`.

Re-publish the same file on later rounds, adding the new draft as a column, so the user sees the progression.

Then tell the user, briefly: the verdict, the page's link, and the brief's gaps the subagent reported, each with a
proposed wording for the brief. Ask: another round, or put it in the game? And which brief fixes to apply.

## 6. Another round

Send the user's notes and your "Still off" list to the same subagent (SendMessage to its agent id), so it fixes its
own draft with its full context. Then back to step 5. Apply the brief fixes the user accepted before the round, and
tell the subagent which sections changed.

## 7. Put it in the game

When the user approves:

1. Move the drafts to the game's sources: `art/unit/` for a playable unit, `art/npc/` for an npc, the same file names.
   `pixel export` both, so their PNGs under `resources/` follow, and delete the draft PNGs (and the drafts' `.aseprite`,
   now moved).
2. Update the brief: the character's Cast row (adding "To improve on a redraw" with what is still off), its line in
   "Redrawing the cast" (or a new one), a new Species row if any, and the brief fixes the user accepted.
3. Run `./gradlew jvmTest --tests '*AssetPaletteArchitectureTest' --tests '*AssetOutlineArchitectureTest'`; fix
   anything red.
4. Commit only those files, with a message describing the character and what changed in the brief. The pre-commit
   hook runs `git add -u`, so any other modified tracked file would be swept in: set it aside first and restore it
   after.
5. Push to `main` (`git push -u origin main`).

A redraw is done: the game already shows it. For a new character, the game does not use it yet: offer, in one line,
to wire it in as a unit or an npc, as a separate request.
