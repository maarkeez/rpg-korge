// Serves the game resources to the JS tests, so the tests read them with `resourcesVfs` as the game does in the browser.
// The resources of the test compilation, with the `$catalog.json` that lists their directories, are processed into
// `build/processedResources/js/test`, and the tests request them from the root (e.g.: `/battlefield/battlefield.toml`).
const path = require("path");
const fs = require("fs");

const resourcesDir = path.resolve(config.basePath, "../../../processedResources/js/test");
if (fs.existsSync(resourcesDir)) {
    config.files.push({ pattern: `${resourcesDir}/**/*`, included: false, served: true, watched: false, nocache: true });
    for (const entry of fs.readdirSync(resourcesDir)) {
        const suffix = fs.statSync(path.join(resourcesDir, entry)).isDirectory() ? "/" : "";
        config.proxies[`/${entry}${suffix}`] = `/absolute${resourcesDir}/${entry}${suffix}`;
    }
}
