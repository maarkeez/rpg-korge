-- The product events the game sends, as it sends them, and when the telemetry collector received them, in milliseconds
-- since 1970. Nothing about the request is kept: no IP address, browser or country.
CREATE TABLE product_events
(
    install_id       TEXT    NOT NULL,
    session_id       TEXT    NOT NULL,
    game_id          TEXT,
    name             TEXT    NOT NULL,
    occurred_at      INTEGER NOT NULL,
    quest_id         TEXT,
    slot             INTEGER,
    lootable_item_id TEXT,
    price            INTEGER,
    unit_id          TEXT,
    received_at      INTEGER NOT NULL,
    -- A batch sent again, as its answer was lost, is ignored
    PRIMARY KEY (install_id, session_id, name, occurred_at)
) WITHOUT ROWID;

-- The nightly deletion of the product events older than the retention period
CREATE INDEX product_events_received_at ON product_events (received_at);
