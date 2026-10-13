// Loads `karma/fetch-retry.js` in the test page before the tests, to retry the fetches that Chrome fails now and then.
config.files.unshift({ pattern: require("path").resolve(config.basePath, "../../../../karma/fetch-retry.js"), included: true, watched: false });
