# Draft: Claude setup review (first-turn token cost)

> **Status:** analysis done, nothing changed yet. The user will decide which changes to make.
> This is a handoff note, not a plan: it does not follow [the plan template](../../.claude/skills/to-plan/template.md).

## The problem

Every Claude Code session in this repository uses **~70–76k input tokens on its first turn**, even for a prompt like
"say just hi" on Haiku. The user mostly drives sessions **from the Claude app via Remote Control** to their Mac.

The user's priorities, in order:

1. **Context window**: the fixed overhead fills the window before any work starts. On a 200k model that is ~37%.
2. **Usage limits.**

The user asked for **analysis only, no changes**, until they decide.

## Target

- Plain Claude Code (built-in tools only, no plugins or MCP): ~15–20k for "say just hi".
- Realistic target for this repo's Remote Control workflow: **~30k**. That covers the built-in tools, the Artifact tool,
  the base prompt and the project skills.

## Evidence

### Measured

- First turn of the 14 latest sessions: 69,262–76,324 tokens. The Haiku "say just hi" session used 73,012.
- In warm sessions, 52–58k of it was a cache read. Caching lowers the cost, but not the space in the context window.
- Parts recorded in the session transcript add up to only ~17–19k tokens:
  - skill listing: 29.8k chars, ~7.5k tokens
  - system prompt: 17k chars, ~4.3k tokens
  - agent listing, MCP instructions, deferred tool names, memory index, git status and hooks: ~5k tokens
- **By subtraction, ~54k tokens are tool definitions.** They are not in the transcript.
- Tool calls across all **125 sessions** of this repo: Bash 18,284, Read 941, Write 708, AskUserQuestion 687, Edit 387,
  Artifact 42, ToolSearch 40, Skill 28, Agent 28, SendUserFile 19. **Claude Code Remote MCP tools: 1 call** (`add_repo`).

How it was measured:

- Transcripts are the `*.jsonl` files under `~/.claude/projects/<this repo>/`. Each line is a JSON entry.
- The first entry with `type == "assistant"` holds `message.usage`. Its total is `input_tokens`,
  `cache_creation_input_tokens` and `cache_read_input_tokens` added up.
- Entries with `type == "attachment"` before it hold the injected context (`skill_listing`, `agent_listing_delta`, …).
- `prompt_snapshot.systemPrompt` is the system prompt.

### Estimated split of the ~54k of tool definitions

These numbers are not measured. Running `/context` at the start of a fresh session gives the exact numbers. The user
declined to do it for now.

| Source | ~Tokens | Needed by this repo? |
|---|---|---|
| **Claude Code Remote** connector: 33 tools, all fully loaded (not deferred) | 15–25k | **No** |
| Artifact tool | ~10k | Yes: the `to-implement` skill shares screenshots with it |
| Built-in tools | 8–10k | Yes |
| Claude Docs connector: 3 tools and its instructions | ~1.5k | No |

### Other contributors

- **System prompt text written for cloud containers**: ~2–3k tokens.
  - It covers ephemeral containers, the GitHub MCP tools, "no `gh` CLI", push retries and disk allowances.
  - It is wrong for Remote Control on the Mac: `gh` is installed and logged in there, and `to-implement` uses
    `gh run list`.
- **Skill listing** (~7.5k):
  - Work plugins unrelated to this repo: ~3k.
    - One plugin, with skills for unrelated backend work, is enabled by the user in `~/.claude/settings.json`: ~1.9k.
    - Two plugins are enforced by an organization-managed settings file, so the user can likely not disable them:
      ~1.2k.
  - Anthropic and general skills (docs, pptx, google-workspace, computer-use, dataviz, claude-api, …): ~4k.
  - The user's diataxis writers in `~/.claude/skills/`: ~0.6k.
  - Project skills: ~0.3k. Only `pixel-art` and `storybook` are listed. The other 5 have
    `disable-model-invocation: true`, so they cost nothing up front.
- **Agent listing**: an agent from one of the managed plugins carries long examples in its description (~500 tokens).
- **A UserPromptSubmit hook** from a managed plugin adds ~200 tokens to every prompt.

### Not contributing

- There is no `CLAUDE.md` in the repo or in `~/.claude`.
- `GLOSSARY.md` is not loaded.
- The memory index is small (~500 tokens).
- MCP servers that fail to connect only add a notice.
- The other claude.ai connectors are deferred, so they cost only their names.

## What the project skills need

The 7 skills are in [`.claude/skills/`](../../.claude/skills/).

- Bash, including `run_in_background` for the Storybook server
- Read, Write and Edit
- AskUserQuestion
- Artifact
- Skill

`to-implement` and `grill-with-docs` forbid subagents. None of the skills uses a Claude Code Remote tool.

## What the user was told about the Claude Code Remote tools

They were explained grouped by feature, and none of them fits the current workflow:

- coordinating other sessions
- scheduled tasks (triggers, `send_later`)
- cloud environments
- connecting a cloud session to a folder on the Mac
- adding repositories
- watching pull requests (the repo pushes straight to `main`)
- webhooks
- `report_preview_port`: marginally useful for the Storybook

## Candidate changes (none approved yet)

1. **Turn off the Claude Code Remote connector for this project.** It is the biggest win.
   - Open risk: it is **unverified** whether this breaks Remote Control from the Claude app.
   - Safe test: disable it for this project only, then start one session from the app.
2. **Disable the user-enabled work plugin** for this project, in `.claude/settings.json` (`enabledPlugins`).
3. **Disable the Claude Docs connector** for this project, if it is not used here.
4. **Re-measure afterwards**, preferably with `/context`, against the ~30k target.
5. If the organization-managed plugins stay a notable cost, the user may raise it with their maintainers.

## Open questions for the user

- Which of the candidate changes to make, and whether per project or user-wide.
- Whether they will run `/context` in a fresh Remote Control session, to replace the estimates with exact numbers.
- Whether to keep the general Anthropic skills (docs, pptx, google-workspace, …) available in this repo.

## Notes for the next agent

- Make changes only once the user picks them.
- Settings live in:
  - `~/.claude/settings.json`
  - `.claude/settings.local.json`
  - `~/.claude.json` (MCP servers per project)
  - the organization-managed settings file (read-only)
- A managed plugin's hook blocks `cd <dir> && git ...`. Use `git -C <dir>` instead.
- If this file gets committed, follow the run-configuration rule in the [`to-implement`](../../.claude/skills/to-implement/SKILL.md) skill's Committing section.

## Suggested skills

- **`update-config`**: for every change to `settings.json`, `settings.local.json`, plugins, MCP servers or hooks. Call
  it before editing any of them.
- No project skill (`to-plan`, `to-implement`, `grill-me`, …) applies: this work does not touch the game.
