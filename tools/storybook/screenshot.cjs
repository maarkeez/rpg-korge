// Opens a story of the Storybook in Chrome, waits until it has rendered, optionally clicks and types on it, and saves a
// screenshot. The Storybook must be served first, e.g.: python3 -m http.server 8765 -d build/www.
// See .claude/skills/storybook/SKILL.md.
//
// Usage: node tools/storybook/screenshot.cjs '<query>' [--out <png>] [--port <port>] [steps...]
//   <query>         What to open, as in a link: 'story=screen-shop--buy&args=gold:0&globals=language:es'.
//   --out <png>     Where to save the last screenshot. Default: build/storybook/<story id>.png.
//   --port <port>   Where the Storybook is served. Default: 8765.
// Steps, run in order, each one followed by a short wait for the story to render again:
//   --click <x>,<y>  Clicks the window, e.g.: a story in the sidebar, a control button, a slot of the shop.
//   --type <text>    Types the text and presses Enter, e.g.: into a text input clicked before, after its text.
//   --replace <text> Deletes the text of the input clicked before, then types the text and presses Enter.
//   --press <key>    Presses a key, e.g.: Backspace, or Meta+A (Control+A on Linux) to select the text of an input.
//   --wait <ms>      Waits, e.g.: for an animation.
//   --shot <png>     Saves a screenshot now, besides the last one.
const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');

// The Storybook window: the page is scaled to fit, so at this size the coordinates of the page are those of the window.
const VIEWPORT = { width: 1280, height: 900 };
// The Storybook titles the page with this and the story id once the story has rendered (see StorybookPage).
const RENDERED_TITLE_PREFIX = 'Storybook – ';
// Loading the bundle and its resources takes about 9 seconds locally, with a software renderer.
const RENDERED_TIMEOUT_MS = 60_000;
// After a step, how long to wait for the story to render again. A step that only logs an action renders nothing.
const STEP_RENDER_TIMEOUT_MS = 1_500;
const SETTLE_MS = 300;
// KorGE's text inputs do not select all their text with a shortcut, so it is deleted a character at a time.
const MAXIMUM_INPUT_LENGTH = 64;
// Chrome draws WebGL in software when there is no GPU, e.g.: headless or in a cloud session.
const CHROME_ARGS = ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'];

const repository = path.resolve(__dirname, '../..');

function parseArguments(argv) {
  const options = { query: null, out: null, port: 8765, steps: [] };
  for (let i = 0; i < argv.length; i++) {
    const argument = argv[i];
    const value = () => {
      if (i + 1 >= argv.length) fail(`${argument} needs a value`);
      return argv[++i];
    };
    if (argument === '--out') options.out = value();
    else if (argument === '--port') options.port = Number(value());
    else if (['--click', '--type', '--replace', '--press', '--wait', '--shot'].includes(argument)) options.steps.push({ kind: argument.slice(2), value: value() });
    else if (argument.startsWith('--')) fail(`Unknown option ${argument}`);
    else if (options.query === null) options.query = argument.replace(/^\?/, '');
    else fail(`Unexpected argument ${argument}`);
  }
  if (options.query === null) fail("Missing the query, e.g.: 'story=screen-shop--buy'");
  return options;
}

function fail(message) {
  console.error(message);
  process.exit(2);
}

// Playwright, with the browsers it installs (e.g.: in a cloud session), or else playwright-core, which drives the
// installed Chrome (`npm install` in tools/storybook).
function loadPlaywright() {
  const candidates = [
    { module: 'playwright', bundledBrowser: true },
    { module: path.join(__dirname, 'node_modules', 'playwright-core'), bundledBrowser: false },
    { module: 'playwright-core', bundledBrowser: false },
  ];
  let globalRoot = null;
  try {
    globalRoot = execSync('npm root -g', { stdio: ['ignore', 'pipe', 'ignore'] }).toString().trim();
  } catch (error) {
    // No npm: only the local and NODE_PATH modules are looked for.
  }
  if (globalRoot) candidates.splice(1, 0, { module: path.join(globalRoot, 'playwright'), bundledBrowser: true });
  for (const candidate of candidates) {
    try {
      return { playwright: require(candidate.module), bundledBrowser: candidate.bundledBrowser };
    } catch (error) {
      // Try the next one.
    }
  }
  fail('Playwright is missing. Install playwright-core, which drives the installed Chrome: cd tools/storybook && npm install');
}

function installedChrome() {
  if (process.env.CHROME) return process.env.CHROME;
  const paths = {
    darwin: ['/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', '/Applications/Chromium.app/Contents/MacOS/Chromium'],
    win32: ['C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'],
    linux: ['/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser'],
  }[process.platform] || [];
  return paths.find((candidate) => fs.existsSync(candidate));
}

async function launch({ playwright, bundledBrowser }) {
  const chrome = installedChrome();
  if (bundledBrowser && !process.env.CHROME) {
    try {
      return await playwright.chromium.launch({ headless: true, args: CHROME_ARGS });
    } catch (error) {
      if (!chrome) throw error;
      console.log(`The Chromium of Playwright did not start, using ${chrome}`);
    }
  }
  if (!chrome) fail('Chrome is missing. Set CHROME to its path.');
  return playwright.chromium.launch({ executablePath: chrome, headless: true, args: CHROME_ARGS });
}

const renderedStoryId = (page) => page.title().then((title) => (title.startsWith(RENDERED_TITLE_PREFIX) ? title.slice(RENDERED_TITLE_PREFIX.length) : null));

async function screenshot(page, file) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  await page.screenshot({ path: file });
  console.log(`Screenshot: ${file}`);
}

// KorGE reads a click from the mouse moving over the view, then a press, so the mouse is moved first and held briefly.
async function click(page, value) {
  const [x, y] = value.split(',').map(Number);
  if (Number.isNaN(x) || Number.isNaN(y)) fail(`--click needs <x>,<y>, e.g.: 400,300, not ${value}`);
  await page.mouse.move(x, y);
  await page.waitForTimeout(100);
  await page.mouse.down();
  await page.waitForTimeout(80);
  await page.mouse.up();
}

async function runStep(page, step) {
  if (step.kind === 'shot') return screenshot(page, path.resolve(step.value));
  if (step.kind === 'wait') return page.waitForTimeout(Number(step.value));
  // The title is cleared, so it shows whether the step rendered the story again.
  const title = await page.title();
  await page.evaluate(() => {
    document.title = '';
  });
  if (step.kind === 'click') await click(page, step.value);
  else if (step.kind === 'type' || step.kind === 'replace') {
    if (step.kind === 'replace') {
      await page.keyboard.press('End');
      for (let i = 0; i < MAXIMUM_INPUT_LENGTH; i++) await page.keyboard.press('Backspace');
    }
    await page.keyboard.type(step.value, { delay: 30 });
    await page.keyboard.press('Enter');
  } else await page.keyboard.press(step.value);
  const rendered = await page
    .waitForFunction((prefix) => document.title.startsWith(prefix), RENDERED_TITLE_PREFIX, { timeout: STEP_RENDER_TIMEOUT_MS })
    .then(() => true, () => false);
  if (!rendered) await page.evaluate((previous) => (document.title = previous), title);
  await page.waitForTimeout(SETTLE_MS);
  console.log(`--${step.kind} ${step.value}: ${rendered ? 'rendered again' : 'nothing rendered again'}, ${page.url()}`);
}

(async () => {
  const options = parseArguments(process.argv.slice(2));
  const requestedStoryId = new URLSearchParams(options.query).get('story');
  const out = path.resolve(options.out || path.join(repository, 'build', 'storybook', `${requestedStoryId || 'storybook'}.png`));
  const url = `http://localhost:${options.port}/storybook/?${options.query}`;

  const browser = await launch(loadPlaywright());
  try {
    const page = await browser.newPage({ viewport: VIEWPORT });
    page.on('console', (message) => {
      if (message.type() === 'error') console.log(`Console error: ${message.text()}`);
    });
    page.on('pageerror', (error) => console.log(`Page error: ${error.message}`));
    try {
      await page.goto(url);
    } catch (error) {
      fail(`${url} did not open: is the Storybook served? python3 -m http.server ${options.port} -d build/www\n${error.message}`);
    }
    try {
      await page.waitForFunction((prefix) => document.title.startsWith(prefix), RENDERED_TITLE_PREFIX, { timeout: RENDERED_TIMEOUT_MS });
    } catch (error) {
      await screenshot(page, out);
      fail(`No story rendered in ${RENDERED_TIMEOUT_MS / 1000} seconds. Is ${url} the Storybook, built with its readiness signal?`);
    }
    await page.waitForTimeout(SETTLE_MS);
    const storyId = await renderedStoryId(page);
    console.log(`Rendered: ${storyId}`);
    if (requestedStoryId && storyId !== requestedStoryId) {
      console.log(`Warning: ${requestedStoryId} is not a story, so the first story is shown. List them in StorySmokeTest.`);
    }
    for (const step of options.steps) await runStep(page, step);
    await screenshot(page, out);
    console.log(`URL: ${page.url()}`);
  } finally {
    await browser.close();
  }
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
