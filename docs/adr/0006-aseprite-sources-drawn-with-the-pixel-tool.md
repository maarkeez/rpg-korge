# Every image has an Aseprite source, drawn with the pixel tool

Every PNG under `resources` has a layered Aseprite source under `art`, at the same path (`art/unit/knight.aseprite` for
`resources/unit/knight.png`), drawn with our own [`pixel`](../../tools/pixel-art/README.md) command line tool, which
exports the PNG from the visible layers every time it saves the source. We chose this so coding agents can draw and
edit the art from the command line, through the `pixel-art` skill, while the layers stay editable by anyone in Aseprite,
and so a source indexed with the palette can only hold valid pixels (see
[ADR 0005](0005-famicube-palette-and-outline.md)). To stay openable in Aseprite, a source keeps a single frame, without
layer groups, blend modes, tags nor slices. The sources are kept out of `resources`, so they are not bundled, and the
drafts under `art/drafts` are never exported.
