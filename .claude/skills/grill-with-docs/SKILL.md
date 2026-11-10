---
name: grill-with-docs
description: A relentless interview to sharpen a plan or design, which also writes the glossary (GLOSSARY.md) and the ADRs (docs/adr/) as terms and decisions are settled, then commits them to main. Only run when the user types /grill-with-docs.
disable-model-invocation: true
---

# Grill with docs

Grill the user about a plan or design until you reach a shared understanding, and build the game's domain model while
you do: challenge the words, write each settled term to [`GLOSSARY.md`](../../../GLOSSARY.md), and record the hard
decisions as ADRs in [`docs/adr/`](../../../docs/adr). Everything happens in this session: no subagents.

## 1. Start

Check the repository: be on `main`, and run `git pull --rebase origin main`. Set aside the run configuration: copy
`.run/runJvmAutoreload.run.xml` to the scratchpad and run `git checkout -- .run/runJvmAutoreload.run.xml`, as the
pre-commit hook runs `git add -u` and it is never committed. If any other change is uncommitted, stop and ask.

Then read `GLOSSARY.md`, the ADRs the topic touches, and the code it touches.

## 2. Interview

Follow the rounds of the [`grill-me` skill](../grill-me/SKILL.md): read it and apply its rules, as it can't be started
from here. Every round is asked with the AskUserQuestion tool, recommended answers first, and facts are found by you,
never asked.

While grilling, actively build and sharpen the domain model:

- **Challenge against the glossary.** When the user uses a term that conflicts with `GLOSSARY.md`, call it out at once:
  "The glossary defines a cast as X, but you seem to mean Y. Which is it?"
- **Sharpen fuzzy language.** When a term is vague or overloaded, propose a precise canonical one: "You're saying
  'target': do you mean the recipient or the targeted tile? Those are different things."
- **Discuss concrete scenarios.** When relationships between concepts come up, stress-test them with specific
  scenarios that probe the edge cases and force the boundaries between concepts to be precise.
- **Cross-reference with the code.** When the user states how something works, check whether the code agrees, and
  surface any contradiction: "The code removes a summon when it vanishes, but you said it stays. Which is right?"
- **Change an existing entry only when the user agrees.** If a word already in the glossary now looks wrong, ask.

## 3. Write as you go

**Terms.** When the user's answer settles a term, write it to `GLOSSARY.md` right away, following
[GLOSSARY-FORMAT.md](GLOSSARY-FORMAT.md). Don't batch terms up for the end. Ask where every new term goes as part of
the question that settles it.

**ADRs.** Offer an ADR only when a decision is all three:

1. **Hard to reverse**: the cost of changing your mind later is meaningful.
2. **Surprising without context**: a future reader will wonder "why did they do it this way?"
3. **The result of a real trade-off**: there were genuine alternatives and one was picked for specific reasons.

If any of the three is missing, skip it. Write the ADR only once the user accepts the offer, following
[ADR-FORMAT.md](ADR-FORMAT.md).

**Never the code.** When a settled term differs from the code, the data or the player text, don't rename anything:
keep it as a follow-up for the end.

## 4. Finish

The session is done when the frontier is empty and the user confirms you have reached a shared understanding. Then:

1. Commit only `GLOSSARY.md` and the new ADRs, in one commit, and check it with `git show --stat HEAD`. The message is
   one sentence, like the rest of `git log`. If nothing was written, skip the commit.
2. Push with `git push -u origin main`.
3. Copy the run configuration back, and also when stopping early.
4. Report the terms added, the entries changed, the ADRs written, and the follow-ups: the renames of code, data and
   player text the glossary now asks for. Suggest `/to-plan` in this same conversation to plan them.

---

Adapted from Matt Pocock's [skills](https://github.com/mattpocock/skills), under the MIT licence.
