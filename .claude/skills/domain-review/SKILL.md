---
name: domain-review
description: Temporary skill to finish GLOSSARY.md. Reviews one subdomain missing from the glossary by interviewing the user, then writes its section and, when there are follow-ups, a docs/plans/<subdomain>-renames.md plan, and commits both to main. Only run when the user asks.
argument-hint: "[subdomain, e.g. battle]"
disable-model-invocation: true
---

# Domain review

Temporary: it only exists to cover the subdomains still missing from [`GLOSSARY.md`](../../../GLOSSARY.md), one per run.
Delete it once none is missing (see [Start](#1-start)).

Settle one word per concept for one subdomain, with the user. Renaming the code is never part of the review: what the
glossary implies for the code goes in a plan, for [`to-implement`](../to-implement/SKILL.md) to build later.

## 1. Start

The subdomains are the packages under `src/commonMain/kotlin/com/mkz/rpg/`. One is missing when `GLOSSARY.md` has no
`###` heading for it, comparing without case or spaces: `### Battle unit` covers `battleUnit`.

- **An argument:** review that subdomain. If it already has a section, stop and say so: this skill does not revise a
  section on its own.
- **No argument:** list the missing subdomains and suggest the first in alphabetical order, ignoring case.
- **None missing:** say the glossary is complete, and offer to delete `.claude/skills/domain-review/` in a commit. Delete
  it only on a yes, and keep `GLOSSARY.md` and the plans.

Then check the repository: be on `main`, and run `git pull --rebase origin main`. Set aside the run configuration (see
[Write and commit](#5-write-and-commit)). If any other change is uncommitted, stop and ask.

## 2. Read before asking

Finding facts is your job, never the user's. Read:

- The whole package: its `domain` (the model, events and errors), `usecases` and `adapters`.
- Its data under `resources/`, and how other subdomains use its words.
- The words players see, in `resources/localization/messages.pot`.
- The docs in `docs/architecture/` that mention it.
- `GLOSSARY.md`.
- The "Not in this plan" section of every `docs/plans/*-renames.md`.

Tell the user what those sections left waiting for this subdomain. This is only a reminder: the interview covers the
package's own terms.

## 3. Interview

Follow the rounds of the [`grill-me` skill](../grill-me/SKILL.md): read it and apply its rules, as it can't be started
from here. Every question gives your recommended answer first, and stands on its own in the AskUserQuestion dialog.

- Look for one concept with several words, one word with several concepts, and words only programmers would use.
- Test each term with a real scenario, using the game's units, abilities and numbers. Check what the user says against
  the code, and say so when they disagree.
- Show the code or data when the user asks, or when a question is hard to follow without it.
- Report anything that looks like a bug. Don't fix it, and don't argue for it in a definition: it becomes an open
  question in the plan.
- If a word already in the glossary now looks wrong, ask. Change that entry only if the user agrees.
- Every package gets a section, however short. Don't offer to skip one.

These rules were settled in earlier reviews. Apply them without asking again:

- **A section holds what its package holds.** A related term whose code lives in another package waits for that section.
- **The best word wins, even when the code differs.** The code's word goes under `_Avoid_`, and the rename in the plan.
- **Player's word vs code's word:** no general rule. Decide term by term, with the user.
- **Definitions say what a thing is.** One or two sentences, without its logic: Cooldown is "the wait an ability imposes
  after a cast before it can be cast again", with no mention of how the turns count.

## 4. Confirm

Write in the conversation the full section, and a summary of the plan: its renames, player text changes, open questions
and what waits for later sections. Ask the user to confirm it. Change what they ask, and ask again. Don't write anything
before they confirm.

## 5. Write and commit

Once confirmed, don't ask again.

**The section.** Add it under `## Language` in `GLOSSARY.md`, in alphabetical order: `### <Subdomain>`, the package name
in words (`### Battle unit`). Follow the format of the existing sections:

```md
**Term**:
A one or two sentence definition.
_Avoid_: Other word, another word
```

- Be opinionated: one word per concept, with the others under `_Avoid_`.
- Only terms specific to this game, never general programming concepts.
- No implementation details: the glossary is not a spec. Group terms under `####` headings when they form clusters.

**The plan.** When there is any follow-up at all, write `docs/plans/<subdomain>-renames.md`, following the
[`to-plan` template](../to-plan/template.md) and its [style](../to-plan/SKILL.md#style), with
`> **Status:** to do.` No follow-up, no plan. `to-plan` can't be started from here, and its decisions are already
settled, so don't interview again. See [`ability-renames.md`](../../../docs/plans/ability-renames.md) for an example.

- It builds only renames and player text changes. Name the real classes, functions, TOML keys, translation keys, tests
  and docs that change.
- Say whether each rename reaches the save format: renaming a field of a snapshot MUST bump `SAVE_VERSION`, see
  [Save and load](../../../docs/architecture/save-and-load.md#save_version). Say which translation keys and catalogs change.
- `## Not in this plan` holds the open questions and, under `### Waiting for other sections`, what later reviews must look
  at, as `` - `<subdomain>`: <what> ``.

A word changed in an earlier section adds its rename to that subdomain's plan if the plan still exists, or to a new plan
for it if not. A plan with its status set to done is treated as gone.

**Commit.** The pre-commit hook runs `git add -u`, so every modified tracked file goes into the commit.

1. Before starting, copy `.run/runJvmAutoreload.run.xml` to the scratchpad and run
   `git checkout -- .run/runJvmAutoreload.run.xml`. Copy it back after the push, and also when stopping early. It is
   never committed.
2. Commit only `GLOSSARY.md` and the plans, in one commit, and check it with `git show --stat HEAD`. The message is one
   sentence, like the rest of `git log`: `Add the language of the <subdomain> subdomain to GLOSSARY.md, <what it settled>,
   and plan its renames in docs/plans/<subdomain>-renames.md`.
3. Push with `git push -u origin main`.

## 6. Report

- The terms added, and any earlier entry changed.
- The plan's path, or that there were no follow-ups.
- The open questions.
- The next missing subdomain, or, when there is none, the offer to delete this skill.
