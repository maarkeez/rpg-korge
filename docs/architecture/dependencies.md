# Subdomain dependencies

[`dependencies.html`](dependencies.html) is an interactive report of the dependencies between the subdomains (see [Package structure](package-structure.md)). Open it in a browser: it loads its graph library from a CDN, so it needs an internet connection.

## What it shows

- **The graph**: one node per subdomain, and one arrow per dependency, from the subdomain that imports to the subdomain it imports from. The thicker the arrow, the more imports.
- **Cycles**: the groups of subdomains that can reach each other, even indirectly (strongly connected components), highlighted in red. The **two-way dependencies** (`a ⇄ b`, subdomains that import each other directly) are listed separately, as they are the shortest cycles.
- **The details**: clicking a subdomain highlights what it depends on (blue) and what uses it (orange). Clicking a dependency lists its imports, grouped by the layer of the importing file (`adapters`, `usecases`, `domain`), with a link to each file.
- **The metrics table**, sortable by any column:

| Metric      | What it means                                                                                                    |
|-------------|------------------------------------------------------------------------------------------------------------------|
| Fan-in      | The number of subdomains that depend on it.                                                                      |
| Fan-out     | The number of subdomains it depends on.                                                                          |
| Instability | `fan-out ÷ (fan-in + fan-out)`, from 0 (stable: many depend on it, it depends on none) to 1 (unstable: it depends on many, none depends on it). Undefined (`–`) for a subdomain without dependencies. |
| Imports in  | The number of `import` lines of other subdomains that point to it.                                               |
| Imports out | The number of its `import` lines that point to other subdomains.                                                 |

`shared` is hidden from the graph by default, as almost every subdomain depends on it. The `Show shared` checkbox brings it back.

## How the dependencies are read

- From the `import` lines of the production code, `src/commonMain`. The tests are not included.
- A file belongs to the subdomain named by the first package segment after `com.mkz.rpg`. So `screen.adapters.presentation.battlefieldHud` is counted as part of `screen`.
- Imports within the same subdomain, and imports from outside `com.mkz.rpg` (e.g.: `korlibs`, `kotlin`), are ignored.
- `Main.kt`, declared directly in `com.mkz.rpg`, is ignored.
- This is exact as long as a subdomain is only used through `import` lines: there are no wildcard imports (`ktlint_standard_no-wildcard-imports`) and no fully qualified names in the code.

## Regenerating it

```shell
./gradlew dependencyReport
```

The `pre-commit` hook (see [`.githooks/pre-commit`](../../.githooks/pre-commit)) regenerates it on every commit, from the working tree, and adds it to the commit. If the generation fails, the commit fails. The report has no timestamp, so it only changes when the dependencies do, and its diff shows which ones changed.

The generator lives in [`buildSrc`](../../buildSrc/src/main/kotlin/dependencyreport), and the page is the template [`dependency-report.html`](../../buildSrc/src/main/resources/dependency-report.html), where the generator embeds the dependencies as JSON. Its tests are not run by `./gradlew jvmTest`:

```shell
./gradlew -p buildSrc test
```
