# Resources ship as a single bundle

Contributors edit the individual files under `resources`, but the build zips them, uncompressed, into one
`resources.zip` (`ResourcesBundleTask`), and only that bundle is packaged, on every target. The game reads it once per
run through `ResourcesBundle`, the only class allowed to use `resourcesVfs` (`ResourcesBundleArchitectureTest`), and
starts downloading it while the menu is displayed. We chose this because each file is a round trip in the browser, so a
new game loads every resource with one request instead of one per file. The individual files and KorGE's
`$catalog.json` are left out of the package, so a file read one by one fails instead of being silently slow.
