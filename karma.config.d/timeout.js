// The end-to-end tests boot the whole game, which takes longer than mocha's default timeout of 2 seconds.
config.set({ client: { ...config.client, mocha: { ...(config.client || {}).mocha, timeout: 30000 } } });
