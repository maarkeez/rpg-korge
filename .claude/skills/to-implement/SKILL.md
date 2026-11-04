---
name: to-implement
description: Implements a plan in docs/plans/ milestone by milestone, without pausing. For each one it implements, makes the tests pass, commits, pushes to main and marks it in the plan. Then it checks CI and shares any screenshots for approval. Only run when the user types /to-implement.
argument-hint: "[path of the plan, e.g. docs/plans/give-gold.md]"
disable-model-invocation: true
---

# To implement

Builds a plan from `docs/plans/`, straight on `main`. Do the work in this conversation, not in subagents: it holds the
reasoning behind the plan. The plan file stays the source of truth, so read it again whenever the conversation has been
summarized.

## 1. Start

**Pick the plan.** Use the path passed as an argument. Otherwise use the plan written in this conversation. Otherwise
ask, listing the plans in `docs/plans/` whose status is not done.

**Check the repository.**

- Be on `main`, then run `git pull --rebase origin main`.
- If `.run/runJvmAutoreload.run.xml` has a local change, set it aside (see [Committing](#committing)). It is never
  committed.
- If the plan file is uncommitted, commit only that file first: `Plan the <topic>: <its decisions in one sentence>`.
- If any other change is uncommitted, stop and ask. Don't stash it or commit it.

**Find where to begin.** Start at the first milestone not marked `✅`. If the git log shows a milestone was finished but
never marked, don't redo it: mark it and move on. A plan with no Milestones section is one milestone, whose `Done when`
is everything the plan describes.

## 2. Build each milestone

Go through every milestone in order. Don't pause or ask for approval between them.

1. **Implement** what the milestone covers, following its entry in the plan and the plan's Reference section, including
   its docs, stories and tests.
2. **Commit** the commits the plan lists for the milestone (see [Committing](#committing)).
3. **Test.** Run the same two commands as CI, and make both pass:

   ```
   ./gradlew jvmTest
   ./gradlew jsTest
   ```

   Fix a failure at its cause, in a new commit or by amending the unpushed one it belongs to.
4. **Check the views.** When the milestone changes a view, take a screenshot of each story it adds or changes with the
   [`storybook` skill](../storybook/SKILL.md) and look at it. Fix what is wrong, then keep the screenshots for the end.
5. **Check `Done when`** against what was built. Whatever the tests can't confirm, such as behaviour you can only see by
   playing the real game, goes in the [final report](#4-finish). Don't claim it was checked.
6. **Push** with `git push -u origin main`.
7. **Mark it** in a separate commit, and push that too:
   - In the milestone's row, set `Status` to `✅` and `Done in` to the short hash of its last commit.
   - Commit message: `Mark the <milestone>, M1 of the <plan> plan, as done`.
   - For a view milestone, write `as built, with its screenshots still to be approved` instead, and change the plan's
     status line to say so, naming the milestone: e.g. built on `main`, the screenshots of M2 still to be approved.
   - In the last Mark commit, write `Mark the <milestone>, M3 of the <plan> plan, and the plan itself as done`, and set the
     plan's status line to done. If screenshots are still to be approved, the plan stays as built until they are.
   - A plan with no milestones has no row: only its status line changes.

## 3. When the plan is wrong

The plan may turn out to be wrong or impossible once the code is in front of you. Decide for yourself how to build it:
classes, approach and how the commits are split can all change. Record each change in a `## Changes while building`
section at the end of the plan, saying what changed and why, and commit it with that milestone's Mark commit.

What the milestone delivers doesn't change. **Stop and ask** when:

- its `Done when` can't be met, or
- the only way forward is to disable, skip or weaken a test.

Leave that milestone unpushed, and say what is committed locally, what is left and what you need decided. Milestones
already pushed stay as they are.

## 4. Finish

**Check CI once.** After the last push, wait for the GitHub Actions runs of that commit to finish, using whatever GitHub
access the session has (e.g. `gh run list --commit <sha>`). Don't check after each milestone.

If a run is red, read its failure, fix it in a new commit on `main`, push and check again. If you can't get it green,
stop and report. Never revert or rewrite history without asking.

**Share the screenshots**, if any, on one private Artifact page, grouped by milestone. Ask the user to approve them:

- Approved: `Mark the <milestone>, M2 of the <plan> plan, as done, approved on screenshots`. Set the plan's status to
  done once nothing is left to approve.
- Changes asked: make them, run both test commands, push, and share new screenshots.

**Report**, in this order:

- The milestones built, with their `Done in` hashes.
- The CI result.
- The changes from the plan, or that there were none.
- What the tests could not confirm, for the user to check.

## Committing

The pre-commit hook formats the code, regenerates `docs/architecture/dependencies.html` and runs `git add -u`, so **every
modified tracked file goes into the commit**, whatever was staged.

- **The run configuration.** At the start, copy `.run/runJvmAutoreload.run.xml` to the scratchpad and run
  `git checkout -- .run/runJvmAutoreload.run.xml`. Copy it back after the last push, and also when stopping early.
- **Several commits from one working tree.** Stage the commit's files, then run
  `git stash push --keep-index -- <the other tracked files>`, commit, and `git stash pop`.
- **After the hook rejects a commit.** The files stay staged, and `stash push --keep-index` would store them too, so the
  later `stash pop` conflicts. Fix the problem and commit again without stashing.
- **After every commit**, run `git show --stat HEAD` to check it holds only what you meant.

Commit messages are one sentence, saying what changed and why, like the rest of `git log`. Commit and push straight to
`main`, with no branches or pull requests.
