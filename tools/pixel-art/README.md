# Pixel art tool

`pixel` draws the game's low resolution art in Aseprite files, for coding agents. It reads and writes the layers of a
sprite pixel by pixel, shows them as grids of characters, and imports from and exports to PNG. It follows the
[Aseprite file format](https://github.com/aseprite/aseprite/blob/main/docs/ase-file-specs.md) but does not need
Aseprite. See [the plan](../../docs/plans/pixel-art-tool.md) for its decisions.

## Running it

```shell
tools/pixel-art/pixel --help
tools/pixel-art/pixel grid art/unit/knight.aseprite
```

The `pixel` launcher builds the tool when its sources changed, then runs it. Every command has a `--help`. Coding
agents follow the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md), which describes the workflow. The
sources of the game's images live under [`art`](../../art), see [assets](../../docs/architecture/assets.md).

## Developing it

It is a standalone Gradle build, which the game's build does not include:

```shell
./gradlew -p tools/pixel-art test lintKotlin
./gradlew -p tools/pixel-art formatKotlin
```

The tests decode the files the tool writes with KorGE's own Aseprite reader, an independent implementation of the
format. The [`pixel-art` workflow](../../.github/workflows/pixel-art.yml) runs them when the tool changes.
