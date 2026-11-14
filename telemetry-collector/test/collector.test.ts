import { createScheduledController } from "cloudflare:test";
import { env } from "cloudflare:workers";
import { beforeEach, describe, expect, it } from "vitest";
import sampleBatch from "../../src/jvmTest/fixtures/telemetry/product-events.json";
import worker from "../src/index";

const ORIGIN = "https://maarkeez.github.io";
const DAY = 86_400_000;
// A request as the Workers runtime hands it to the telemetry collector
const IncomingRequest = Request<unknown, IncomingRequestCfProperties>;

async function send(body: unknown, origin: string = ORIGIN): Promise<Response> {
  const request = new IncomingRequest("https://telemetry-collector.example.workers.dev/product-events", {
    method: "POST",
    headers: { "Content-Type": "application/json", Origin: origin, "CF-Connecting-IP": "203.0.113.7" },
    body: JSON.stringify(body),
  });
  return worker.fetch(request, env);
}

async function runNightly(scheduledTime: number): Promise<void> {
  await worker.scheduled(createScheduledController({ scheduledTime, cron: "17 3 * * *" }), env);
}

async function rows(): Promise<Record<string, unknown>[]> {
  const { results } = await env.DB.prepare("SELECT * FROM product_events ORDER BY occurred_at").all();
  return results;
}

async function insertReceivedAt(receivedAt: number, occurredAt: number): Promise<void> {
  await env.DB.prepare(
    "INSERT INTO product_events (install_id, session_id, name, occurred_at, received_at) VALUES (?, ?, 'GAME_OVER', ?, ?)",
  )
    .bind("0f1e2d3c4b5a69788796a5b4c3d2e1f0", "a1b2c3d4e5f60718293a4b5c6d7e8f90", occurredAt, receivedAt)
    .run();
}

beforeEach(async () => {
  await env.DB.exec("DELETE FROM product_events");
});

describe("the telemetry collector", () => {
  it("the telemetry collector should keep every product event of the sample batch", async () => {
    // When
    const response = await send(sampleBatch);
    // Then
    expect(response.status).toBe(204);
    const kept = await rows();
    expect(kept.map((row) => row.name)).toEqual(sampleBatch.productEvents.map((productEvent) => productEvent.name));
    expect(kept.find((row) => row.name === "ITEM_BOUGHT")).toMatchObject({
      lootable_item_id: "health-potion",
      price: 10,
      game_id: "00112233445566778899aabbccddeeff",
    });
  });

  it("the telemetry collector should ignore a batch it already kept", async () => {
    // Given
    await send(sampleBatch);
    // When
    const response = await send(sampleBatch);
    // Then
    expect(response.status).toBe(204);
    expect(await rows()).toHaveLength(13);
  });

  it("the telemetry collector should reject a product event with an unknown name", async () => {
    // Given
    const [first] = sampleBatch.productEvents;
    // When
    const response = await send({ productEvents: [{ ...first, name: "LEVEL_UP" }] });
    // Then
    expect(response.status).toBe(400);
    expect(await rows()).toHaveLength(0);
  });

  it("the telemetry collector should reject a batch of 101 product events", async () => {
    // Given
    const [first] = sampleBatch.productEvents;
    const productEvents = Array.from({ length: 101 }, (_, index) => ({ ...first, occurredAt: index }));
    // When
    const response = await send({ productEvents });
    // Then
    expect(response.status).toBe(400);
    expect(await rows()).toHaveLength(0);
  });

  it("the telemetry collector should reject another origin", async () => {
    // When
    const response = await send(sampleBatch, "https://example.com");
    // Then
    expect(response.status).toBe(403);
    expect(await rows()).toHaveLength(0);
  });

  it("the telemetry collector should keep nothing but the product events and when they were received", async () => {
    // When
    await send(sampleBatch);
    // Then
    const { results } = await env.DB.prepare("SELECT name FROM pragma_table_info('product_events')").all();
    expect(results.map((column) => column.name)).toEqual([
      "install_id",
      "session_id",
      "game_id",
      "name",
      "occurred_at",
      "quest_id",
      "slot",
      "lootable_item_id",
      "price",
      "unit_id",
      "received_at",
    ]);
    expect(JSON.stringify(await rows())).not.toContain("203.0.113.7");
  });

  it("the telemetry collector should delete the product events received more than 90 days ago", async () => {
    // Given
    const now = Date.UTC(2026, 10, 14, 3, 17);
    await insertReceivedAt(now - 91 * DAY, 1);
    // When
    await runNightly(now);
    // Then
    expect(await rows()).toHaveLength(0);
  });

  it("the telemetry collector should keep the product events received 89 days ago", async () => {
    // Given
    const now = Date.UTC(2026, 10, 14, 3, 17);
    await insertReceivedAt(now - 89 * DAY, 1);
    // When
    await runNightly(now);
    // Then
    expect(await rows()).toHaveLength(1);
  });
});
