// Retries the fetches that Chrome rejects right away with "Failed to fetch", without sending them, now and then while the
// JS tests run (e.g.: when an end-to-end test loads the game resources). Chrome keeps rejecting them for a short while, so
// each retry waits twice as long as the previous one. Loaded in the test page before the tests.
(function () {
    const originalFetch = window.fetch;
    const retryDelaysMillis = [50, 100, 200, 400, 800];
    const wait = (millis) => new Promise((resolve) => setTimeout(resolve, millis));

    window.fetch = function (input, init) {
        const url = typeof input === "string" ? input : input.url;
        const attempt = (retry) =>
            originalFetch.call(window, input, init).catch((error) => {
                if (retry >= retryDelaysMillis.length || !(error instanceof TypeError)) throw error;
                console.warn(`Retrying the fetch of ${url} in ${retryDelaysMillis[retry]}ms after: ${error}\n`);
                return wait(retryDelaysMillis[retry]).then(() => attempt(retry + 1));
            });
        return attempt(0);
    };
})();
