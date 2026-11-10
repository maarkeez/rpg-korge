# A quest owns the lines its npcs say about it

An npc says a different line as a quest goes on: its quest giver has a start line, a reminder line and a completed or
start again line, and the npc of a talk task has a line for the talk that does it. These quest lines belong to `quest`:
it answers which one an npc says now, as it does for the quest markers, and their messages are keyed under
`quest.<quest id>`, next to the texts of its tasks. `screen` falls back to the npc's own line, `npc.<npc id>.line`, when
no quest has a line for it. We chose this over each npc listing every line it says so `npc` and `quest` stay
independent, as [ADR 0002](0002-acyclic-shrink-only-subdomain-dependencies.md) asks, and so everything a quest tells
sits in one place.

## Consequences

- A quest giver has no own line, as its side quest has a quest line for it whatever its status: there is no
  `npc.farmer.line`.
- Every quest line is required, which a resource test checks in CI, like the texts of the tasks, and nothing declares
  them in the data: they follow from the quest giver, the talk tasks and whether the quest is repeatable.
- A talk does only what its line says, one talk task or one start, so no quest starts with its start line unread.
