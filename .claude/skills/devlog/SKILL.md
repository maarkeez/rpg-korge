---
name: devlog
description: Interviews the user, with clickable suggested answers, to write a new entry of the game's devlog in docs/devlog/ following the devlog template, or to turn an existing seed entry into a full one.
disable-model-invocation: true
---

# Devlog

The devlog tells the story of building the game, one entry at a time, in [`docs/devlog/`](../../../docs/devlog/). You
are an interviewer: you ask the user, round after round, everything needed to write the entry, then you write it. Every
entry follows the [template](template.md).

The game, in the user's words: a tactical turn based battle game where the battlefield tiles form a 2D grid. Each player
has multiple battle units. Each battle unit has multiple abilities and each ability a set of effects. The core mechanic
is chaining effects on your enemies until the player defeats them.

## 1. Read before asking

- The [template](template.md).
- Every entry in `docs/devlog/`: what they cover (never repeat a topic), their numbers, and how the user writes (see
  [Voice](#voice)).
- What was built: `git log`, and the commits around the topic with `git show --stat <hash>`. Then the plans in
  `docs/plans/` and the architecture in `docs/architecture/` the commits touch. A link to a GitHub file of this
  repository (`github.com/maarkeez/rpg-korge/blob/<hash>/<path>`) is read locally with `git show <hash>:<path>`.

Finding facts is your job, never the user's: don't ask anything the repository answers. Use what you find to word the
suggested answers, so the user mostly clicks.

## 2. Pick what the entry is about

What follows `/devlog` decides it:

- **An idea** (e.g. `/devlog the health bar lingering instead of flashing`): a new entry about it.
- **The number of an entry already in `docs/devlog/`** (e.g. `/devlog 015`): rewrite that entry, in its own file. A seed
  (only notes, commit hashes and plan links, no prose) is the starting material: read everything it points to. A
  finished entry is the first draft: ask what to change.
- **Nothing**: suggest 2–3 topics from the commits and plans since the date and commit of the last entry that no entry
  covers yet, as the first question.

## 3. Interview, a round per section

Ask with the AskUserQuestion tool. Every question gives your recommended answer as the first option, its label ending
in `(Recommended)`, and 1–3 alternatives, each with a short description. The tool always adds a free-text "Other"
option, so don't add one yourself. Each question must stand on its own: the user may only see the dialog. It takes up to
4 questions per call; ask several calls in a row for a larger round.

Work through the rounds in order. Each answer reshapes the next round: skip what is already settled, and ask again what
an answer left unclear.

1. **Story**: the topic and its angle, the kind of story (Feature, Problem, Decision, Lesson or Experiment), and the
   latest commit of the changes once they were finished (for the frontmatter).
2. **Hook**: the opening question or moment (a playtest, a bug, a surprise), and the one sentence the entry is about.
3. **Context**: where it fits in the game, what existed before, the goal and the deliberate scope.
4. **Main story**: why, the alternatives considered and why each was dropped, the final approach, the tradeoffs and
   what is still broken or unfinished. Take the alternatives from the plans and commits.
5. **Key takeaways**: 3–5 lessons. Suggest candidates as a multi-select question.
6. **What's next**: the next challenge. Suggest plans in `docs/plans/` that are not done yet.

From the main story round onwards, end every round with a question offering to write the first draft now, or to keep
asking.

## 4. Write the draft

Write the entry to `docs/devlog/NNN-slug.md`: `NNN` is the highest number in `docs/devlog/` plus one, three digits, and
`slug` is the title in lowercase with dashes (e.g. `017-health-bars-that-linger.md`). A rewrite keeps its own file.
Start with the frontmatter and heading of the [template](template.md); the `date` is today.

Keep to 700–900 words (check with `wc -w`). Then write the whole entry in the conversation too, so the user can read it
there, followed by its path and word count.

## 5. Revise

Ask what to change, with options such as: it is good as it is, shorten it, rewrite the hook, change the tone, rework a
section. Edit the same file, write the whole revised entry in the conversation again, and ask again, until the user is
happy with it.

## 6. Offer to commit

Ask whether to commit the entry. On a yes, commit only the entry's file, with a message in the style of the repository's
recent commits (e.g. `Write the devlog entry about ...`).

## Voice

How the user writes, to keep every entry sounding like the others:

- First person, as a solo developer talking to other developers. Plain, conversational English.
- Short paragraphs, often a single sentence, for rhythm: "It worked." "The prototype was becoming boring."
- One key sentence in **bold** per section or so, the one a reader should remember.
- The questions the game, the CPU or the player asks itself as blockquotes: `> Should I move, cast, or do nothing?`
- Concrete examples: the actual units, abilities, terrains, numbers and measurements, and small `text` diagrams or code
  when they explain better than prose.
- Honest about what is still broken, unfinished or a rabbit hole avoided.
- Links back to earlier entries when the story builds on them ("In my previous work on the CPU brain...").
- Key takeaways start with a short imperative or claim, often bold, followed by one sentence.
- What's next ends on the bigger principle the entry taught.
