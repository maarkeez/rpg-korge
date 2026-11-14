# GLOSSARY.md Format

The game has a single context: one [`GLOSSARY.md`](../../../GLOSSARY.md) at the repository root, with no
`GLOSSARY-MAP.md`.

## Structure

```md
# Tessavar

{One or two sentence description of the game.}

## Language

### Ability

**Ability**:
An action a battle unit can cast during a battle, defined once and shared by every battle unit that knows it.
_Avoid_: Spell, skill, attack, move

#### Targeting

**Targeting**:
An ability's rule for where it can be cast.
_Avoid_: Target
```

- Every subdomain has a `### <Subdomain>` section under `## Language`, in alphabetical order. The subdomains are the
  packages under `src/commonMain/kotlin/com/mkz/tessavar/`, and a section is named after its package in words:
  `### Battle unit` for `battleUnit`.
- Group terms under `####` headings within a section when natural clusters emerge.
- A term is written as `**Term**:`, its definition on the next line, and `_Avoid_:` with the other words, if any.

## Rules

- **Be opinionated.** One word per concept: when several words exist for it, pick the best one and list the others
  under `_Avoid_`.
- **Keep definitions tight, and say what a thing is.** One or two sentences, without its logic: Cooldown is "the wait an
  ability imposes after a cast before it can be cast again", with no mention of how the turns count.
- **Only terms specific to this game.** General programming concepts don't belong, even if the code uses them a lot.
- **No implementation details.** The glossary is not a spec, a scratch pad, or a record of implementation decisions.
- **A section holds what its package holds.** A related term whose code lives in another package goes in that
  package's section.
- **The best word wins, even when the code differs.** The code's word goes under `_Avoid_`, and the rename becomes a
  follow-up: the glossary never waits for the code.
- **Player's word vs code's word:** no general rule. Decide term by term, with the user.
- **Where a new term goes is the user's call.** Ask which section, and which `####` cluster, every new term goes in.
- **An existing entry changes only when the user agrees.**
