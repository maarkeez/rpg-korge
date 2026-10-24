// Plays the web build in Chrome, walking the party with the virtual pad, and reports the frames that took too long.
// See docs/testing/frame-times.md.
import fs from 'node:fs';
import http from 'node:http';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';

// A frame longer than this is reported: about 3 frames at 60 frames per second, a stutter anyone notices.
const LONG_FRAME_MS = 50;
// How long the party walks: one tile west, then one tile east, in turns, so it stays on its first tiles, away from the
// CPU battle units (a battle would pause the exploration).
const WALK_MS = 15_000;
const LEG_MS = 200;
const PAUSE_BETWEEN_LEGS_MS = 150;
// The game window, and where the play button, the arrows and a corner of the up arrow (blue while the pad is displayed)
// are on it.
const GAME_WINDOW = { width: 390, height: 844 };
const PLAY_BUTTON = [195, 398.5];
const ARROWS = { EAST: [189, 598], WEST: [45, 598] };
const PAD_CORNER = [90, 500];
// How long the game takes to load, and to start a new game once play is clicked.
const LOADING_MS = 6_000;
const STARTING_MS = 4_000;

const repository = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const profile = process.argv.includes('--profile');
const chrome =
  process.env.CHROME ||
  {
    darwin: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    win32: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
  }[process.platform] ||
  '/usr/bin/google-chrome';

const sleep = (millis) => new Promise((resolve) => setTimeout(resolve, millis));

// The web build is its resources and its bundle, side by side, as the game is deployed.
function copyWebBuild() {
  const sources = ['build/processedResources/js/main', 'build/kotlin-webpack/js/productionExecutable'].map((source) => path.join(repository, source));
  const missing = sources.filter((source) => !fs.existsSync(source));
  if (missing.length > 0) {
    console.error(`The web build is missing (${missing.join(', ')}). Build it first: ./gradlew jsBrowserProductionWebpack`);
    process.exit(2);
  }
  const folder = fs.mkdtempSync(path.join(os.tmpdir(), 'frame-times-'));
  sources.forEach((source) => fs.cpSync(source, folder, { recursive: true }));
  return folder;
}

const contentTypes = { '.html': 'text/html', '.js': 'text/javascript', '.json': 'application/json', '.png': 'image/png', '.wasm': 'application/wasm', '.css': 'text/css' };

function serve(folder) {
  const server = http.createServer((request, response) => {
    const file = path.join(folder, decodeURIComponent(new URL(request.url, 'http://localhost').pathname));
    if (!file.startsWith(folder) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
      response.writeHead(404).end();
      return;
    }
    response.writeHead(200, { 'Content-Type': contentTypes[path.extname(file)] || 'application/octet-stream' });
    fs.createReadStream(file).pipe(response);
  });
  return new Promise((resolve) => server.listen(0, '127.0.0.1', () => resolve(server)));
}

// A press long enough for the game to see it.
async function tap(page, [x, y]) {
  await page.mouse.move(x, y);
  await page.mouse.down();
  await sleep(120);
  await page.mouse.up();
}

// Whether the virtual pad is displayed, so the party is still exploring: the corner of its up arrow is blue.
async function isPadDisplayed(page) {
  const [x, y] = PAD_CORNER;
  const pixel = await page.screenshot({ encoding: 'base64', clip: { x, y, width: 1, height: 1 } });
  return page.evaluate(async (data) => {
    const image = new Image();
    image.src = `data:image/png;base64,${data}`;
    await image.decode();
    const context = Object.assign(document.createElement('canvas'), { width: image.width, height: image.height }).getContext('2d');
    context.drawImage(image, 0, 0);
    const [red, , blue] = context.getImageData(0, 0, 1, 1).data;
    return blue > 200 && red < 150;
  }, pixel);
}

async function hold(page, [x, y], millis) {
  await page.mouse.move(x, y);
  await page.mouse.down();
  await sleep(millis);
  await page.mouse.up();
}

const folder = copyWebBuild();
const server = await serve(folder);
const browser = await puppeteer.launch({
  executablePath: chrome,
  headless: true,
  // Draws with the GPU, as when playing, rather than with the CPU.
  args: ['--enable-gpu', '--ignore-gpu-blocklist', ...(process.platform === 'darwin' ? ['--use-angle=metal'] : [])],
  defaultViewport: { ...GAME_WINDOW, deviceScaleFactor: 2 },
});
try {
  const page = await browser.newPage();
  // Records when every frame starts, and how long since the previous one.
  await page.evaluateOnNewDocument(() => {
    window.frameTimes = [];
    let previous = performance.now();
    const record = (now) => {
      window.frameTimes.push([now, now - previous]);
      previous = now;
      requestAnimationFrame(record);
    };
    requestAnimationFrame(record);
  });
  await page.goto(`http://127.0.0.1:${server.address().port}/index.html`);
  await sleep(LOADING_MS);
  const renderer = await page.evaluate(() => {
    const gl = document.createElement('canvas').getContext('webgl');
    const info = gl && gl.getExtension('WEBGL_debug_renderer_info');
    return info ? gl.getParameter(info.UNMASKED_RENDERER_WEBGL) : 'unknown';
  });
  await tap(page, PLAY_BUTTON);
  await sleep(STARTING_MS);
  if (!(await isPadDisplayed(page))) throw new Error('The game did not start exploring: have the play button or the virtual pad moved?');

  const cdp = await page.target().createCDPSession();
  if (profile) {
    await cdp.send('Profiler.enable');
    await cdp.send('Profiler.setSamplingInterval', { interval: 500 });
    await cdp.send('Profiler.start');
  }
  const walkStart = await page.evaluate(() => performance.now());
  for (let leg = 0; leg * (LEG_MS + PAUSE_BETWEEN_LEGS_MS) < WALK_MS; leg++) {
    await hold(page, leg % 2 === 0 ? ARROWS.WEST : ARROWS.EAST, LEG_MS);
    await sleep(PAUSE_BETWEEN_LEGS_MS);
  }
  const frames = await page.evaluate((start) => window.frameTimes.filter(([time]) => time >= start), walkStart);
  if (profile) {
    const { profile: cpuProfile } = await cdp.send('Profiler.stop');
    fs.writeFileSync('frame-times.cpuprofile', JSON.stringify(cpuProfile));
  }
  const isStillExploring = await isPadDisplayed(page);

  const durations = frames.map(([, duration]) => duration).sort((a, b) => a - b);
  const percentile = (p) => durations[Math.floor((durations.length - 1) * p)].toFixed(1);
  const longFrames = frames.filter(([, duration]) => duration > LONG_FRAME_MS);
  console.log(`Renderer: ${renderer}`);
  console.log(`Frames: ${frames.length}, p50 ${percentile(0.5)}ms, p95 ${percentile(0.95)}ms, max ${durations[durations.length - 1].toFixed(1)}ms`);
  longFrames.forEach(([time, duration]) => console.log(`Long frame at ${((time - walkStart) / 1000).toFixed(2)}s: ${duration.toFixed(0)}ms`));
  if (profile) console.log('CPU profile: frame-times.cpuprofile (open it in the Performance panel of Chrome DevTools)');
  if (!isStillExploring) {
    console.log('The party stopped exploring meanwhile (e.g.: a CPU battle unit engaged it), so the CPU battle units no longer wandered: measure again');
    process.exitCode = 2;
  } else if (longFrames.length > 0) {
    console.log(`${longFrames.length} frames over ${LONG_FRAME_MS}ms`);
    process.exitCode = 1;
  } else {
    console.log(`No frame over ${LONG_FRAME_MS}ms`);
  }
} catch (error) {
  console.error(error.message);
  process.exitCode = 2;
} finally {
  await browser.close();
  server.close();
  fs.rmSync(folder, { recursive: true, force: true });
}
