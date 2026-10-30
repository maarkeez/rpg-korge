---
name: to-plan
description: Turns the current conversation into a plan in docs/plans/, following its template (Status and Summary, a Decisions table, a section per subdomain or class, Storybook, Docs, Tests, and Milestones when the work spans several steps). Only run when the user types /to-plan.
disable-model-invocation: true
---

# To plan

Writes what was settled in this conversation as a new plan in `docs/plans/`, following its template.
It only creates new plans: it never updates an existing plan as built.

The game is an RPG battle sim in Kotlin and KorGE. The player controls a team of battle units against the CPU's battle
units. The code is split into subdomains, each with `domain`, `usecases` and `adapters` packages.

## 1. Read before asking

- The [template](template.md).
- Any plan in `docs/plans/` the topic builds on or replaces.
- The docs in `docs/architecture/` the topic touches.
- The code the plan touches: the subdomains, classes, functions, events, views, stories and tests involved. The plan
  names real ones (e.g. `BattlefieldView.moveBattleUnit`, `BattlefieldPresenterTest`), never made-up ones.

Finding facts is your job, never the user's: don't ask anything the repository answers.

## 2. Gather the conversation

From the conversation so far, collect:

- **The topic**: what changes, and its title.
- **The problem**: what happens today, with a concrete example. Use the units, screens and numbers.
- **The decisions**: every choice settled, the alternatives dropped and why, and what is deliberately out of scope.
- **The pieces**: the subdomains and classes that change, the stories, the docs and the tests.

## 3. Ask about the gaps

List the decisions the plan needs that the conversation left open. Usually these are the scope, the behaviour in the
edge cases, where it lives in the architecture, the stories, the tests, and what is out of scope.

Ask only those, with the AskUserQuestion tool. Every question gives your recommended answer as the first option, its
label ending in `(Recommended)`, and 1–3 alternatives, each with a short description. The tool always adds a free-text
"Other" option, so don't add one yourself. Each question must stand on its own: the user may only see the dialog. It
takes up to 4 questions per call; ask several calls in a row for more. Ask again when an answer opens a new gap.

Skip this step when nothing is open.

## 4. Pick the shape

- **Small** (the default): Decisions, then a section per subdomain or class, Storybook, Docs, Tests and Known
  limitations.
- **Milestones**: only when the work spans several deliverable steps, each green on CI. Use Decisions, a Milestones
  table, the commits of each milestone, and a Reference section holding the per-component detail. Every milestone starts as `⬜` with an empty `Done in`.

Leave out any section with nothing to say (e.g. no Storybook section when no view changes).

## 5. Write the plan

Write it to `docs/plans/<slug>.md`: `slug` is the topic in lowercase with dashes (e.g. `lingering-health-flash.md`),
with no number. Don't overwrite a plan that already exists: pick another slug. Follow the [template](template.md), with
`> **Status:** to do.`

Then write the whole plan in the conversation too, so the user can read it there, followed by its path.

## 6. Revise

Ask what to change, with options such as: it is good as it is, add a decision, rework a section, split it into
milestones. Edit the same file, write the whole revised plan in the conversation again, and ask again, until the user is
happy with it.

## 7. Offer to commit

Ask whether to commit the plan. On a yes:

- The pre-commit hook runs `git add -u`, so first set aside any local change to `.run/runJvmAutoreload.run.xml`.
- Commit only the plan's file, with a message in the style of the repository's earlier plan commits. Start with
  `Plan the ...` and summarise the decisions in one sentence (e.g. `Plan the search of the Storybook sidebar: a box
  under the title that lists only the stories whose group, component and name hold every word typed, kept in the
  presentation only`).

## Style

How the plans are written, to keep each one sounding like the others:

- Plain English, in the present tense, as if the change were already built: "`hold()` stops the blinks".
- Concrete: the actual units, terrains, items, numbers, durations, colors and constants (e.g. `HEALTH_FLASH_DURATION`,
  1.5s, `#3A3A3A`). Examples start with "E.g.:".
- In the Decisions table, each topic is one or two sentences. When an alternative was dropped, give it and why: "Chosen
  over ..., as ...".
- Code names (classes, functions, events, story ids) go in backticks. Stories are in bold as
  **Group / Component › Story**.
- Test names follow the house style: `<Subject> should <outcome> when <condition>`, grouped under their test class.
- Link the plans and the architecture docs it builds on with relative links (`../architecture/<doc>.md`).
- Tables are aligned.
