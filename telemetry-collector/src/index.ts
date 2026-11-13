// The telemetry collector: answers `POST /product-events` from the game deployed to GitHub Pages, and deletes every night
// the product events received more than 90 days ago. See README.md.
import { parseBatch, type ProductEvent } from "./productEvents";

const MAX_REQUEST_BYTES = 64 * 1024;

const RETENTION_DAYS = 90;

const MILLISECONDS_PER_DAY = 86_400_000;

const PREFLIGHT_MAX_AGE_SECONDS = 86_400;

export default {
  async fetch(request, env): Promise<Response> {
    const url = new URL(request.url);
    if (url.pathname !== "/product-events") return answer(env, 404);
    const origin = request.headers.get("Origin");
    if (request.method === "OPTIONS") return preflight(env, origin);
    if (request.method !== "POST") return answer(env, 405, { Allow: "POST, OPTIONS" });
    if (!isAllowed(env, origin)) return answer(env, 403);
    // The IP address is only the key of the rate limit: it is never stored
    const { success } = await env.RATE_LIMITER.limit({ key: request.headers.get("CF-Connecting-IP") ?? "unknown" });
    if (!success) return answer(env, 429);
    const body = await readBody(request);
    if (body === null) return answer(env, 400);
    const batch = parseBatch(body);
    if ("rejected" in batch) return answer(env, 400);
    try {
      await keep(env.DB, batch.productEvents, Date.now());
    } catch (error) {
      console.error("D1 failed to keep a batch", error);
      return answer(env, 500);
    }
    return answer(env, 204);
  },

  async scheduled(controller, env): Promise<void> {
    await deleteExpired(env.DB, controller.scheduledTime);
  },
} satisfies ExportedHandler<Env>;

async function deleteExpired(db: D1Database, now: number): Promise<void> {
  await db
    .prepare("DELETE FROM product_events WHERE received_at < ?")
    .bind(now - RETENTION_DAYS * MILLISECONDS_PER_DAY)
    .run();
}

async function keep(db: D1Database, productEvents: ProductEvent[], receivedAt: number): Promise<void> {
  const insert = db.prepare(
    `INSERT OR IGNORE INTO product_events
       (install_id, session_id, game_id, name, occurred_at, quest_id, slot, lootable_item_id, price, unit_id, received_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
  );
  await db.batch(
    productEvents.map((productEvent) =>
      insert.bind(
        productEvent.installId,
        productEvent.sessionId,
        productEvent.gameId,
        productEvent.name,
        productEvent.occurredAt,
        productEvent.questId,
        productEvent.slot,
        productEvent.lootableItemId,
        productEvent.price,
        productEvent.unitId,
        receivedAt,
      ),
    ),
  );
}

async function readBody(request: Request): Promise<unknown | null> {
  const declaredLength = Number(request.headers.get("Content-Length") ?? "0");
  if (declaredLength > MAX_REQUEST_BYTES) return null;
  const bytes = await request.arrayBuffer();
  if (bytes.byteLength > MAX_REQUEST_BYTES) return null;
  try {
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch {
    return null;
  }
}

function isAllowed(env: Env, origin: string | null): boolean {
  return env.ALLOWED_ORIGIN === "*" || origin === env.ALLOWED_ORIGIN;
}

function preflight(env: Env, origin: string | null): Response {
  if (!isAllowed(env, origin)) return answer(env, 403);
  return answer(env, 204, {
    "Access-Control-Allow-Methods": "POST",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Max-Age": String(PREFLIGHT_MAX_AGE_SECONDS),
  });
}

function answer(env: Env, status: number, headers: Record<string, string> = {}): Response {
  return new Response(null, {
    status,
    headers: { "Access-Control-Allow-Origin": env.ALLOWED_ORIGIN, Vary: "Origin", ...headers },
  });
}
