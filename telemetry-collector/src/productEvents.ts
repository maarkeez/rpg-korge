// The batch of product events the game sends, as `src/jvmTest/fixtures/telemetry/product-events.json` holds it.

export const MAX_BATCH_SIZE = 100;

const MAX_TEXT_LENGTH = 64;

export const PRODUCT_EVENT_NAMES = [
  "MAIN_MENU_OPENED",
  "GAME_STARTED",
  "GAME_LOADED",
  "GAME_SAVED",
  "QUEST_STARTED",
  "QUEST_COMPLETED",
  "QUEST_ABANDONED",
  "BATTLE_STARTED",
  "BATTLE_FINISHED",
  "GAME_OVER",
  "ITEM_BOUGHT",
  "ITEM_SOLD",
  "MEMBER_RECRUITED",
] as const;

export type ProductEventName = (typeof PRODUCT_EVENT_NAMES)[number];

export interface ProductEvent {
  name: ProductEventName;
  installId: string;
  sessionId: string;
  gameId: string | null;
  occurredAt: number;
  questId: string | null;
  slot: number | null;
  lootableItemId: string | null;
  price: number | null;
  unitId: string | null;
}

export type ParsedBatch = { productEvents: ProductEvent[] } | { rejected: string };

const FIELDS = [
  "name",
  "installId",
  "sessionId",
  "gameId",
  "occurredAt",
  "questId",
  "slot",
  "lootableItemId",
  "price",
  "unitId",
] as const satisfies readonly (keyof ProductEvent)[];

const ID = /^[0-9a-f]{32}$/;

/** Returns the product events of a batch, or why it is rejected. */
export function parseBatch(body: unknown): ParsedBatch {
  if (!isObject(body) || !hasExactly(body, ["productEvents"])) return { rejected: "the batch is not {\"productEvents\": [...]}" };
  const productEvents = body.productEvents;
  if (!Array.isArray(productEvents)) return { rejected: "productEvents is not a list" };
  if (productEvents.length === 0) return { rejected: "the batch is empty" };
  if (productEvents.length > MAX_BATCH_SIZE) return { rejected: `the batch holds more than ${MAX_BATCH_SIZE} product events` };
  const parsed: ProductEvent[] = [];
  for (const [index, productEvent] of productEvents.entries()) {
    const reason = rejectionOf(productEvent);
    if (reason !== null) return { rejected: `product event ${index}: ${reason}` };
    parsed.push(productEvent as ProductEvent);
  }
  return { productEvents: parsed };
}

function rejectionOf(productEvent: unknown): string | null {
  if (!isObject(productEvent) || !hasExactly(productEvent, FIELDS)) return `its fields are not ${FIELDS.join(", ")}`;
  const { name, installId, sessionId, gameId, occurredAt, questId, slot, lootableItemId, price, unitId } = productEvent;
  if (!PRODUCT_EVENT_NAMES.includes(name as ProductEventName)) return "unknown name";
  if (!isId(installId)) return "installId is not 32 lowercase hex digits";
  if (!isId(sessionId)) return "sessionId is not 32 lowercase hex digits";
  if (gameId !== null && !isId(gameId)) return "gameId is neither null nor 32 lowercase hex digits";
  if (typeof occurredAt !== "number" || !Number.isFinite(occurredAt) || occurredAt < 0) return "occurredAt is not a time";
  if (!isTextOrNull(questId)) return "questId is not a short text or null";
  if (!isIntegerOrNull(slot)) return "slot is not a whole number or null";
  if (!isTextOrNull(lootableItemId)) return "lootableItemId is not a short text or null";
  if (!isIntegerOrNull(price)) return "price is not a whole number or null";
  if (!isTextOrNull(unitId)) return "unitId is not a short text or null";
  return null;
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function hasExactly(value: Record<string, unknown>, fields: readonly string[]): boolean {
  const keys = Object.keys(value);
  return keys.length === fields.length && fields.every((field) => Object.hasOwn(value, field));
}

function isId(value: unknown): value is string {
  return typeof value === "string" && ID.test(value);
}

function isTextOrNull(value: unknown): boolean {
  return value === null || (typeof value === "string" && value.length > 0 && value.length <= MAX_TEXT_LENGTH);
}

function isIntegerOrNull(value: unknown): boolean {
  return value === null || Number.isSafeInteger(value);
}
