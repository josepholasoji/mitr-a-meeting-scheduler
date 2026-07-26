CREATE TABLE time_slots
(
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ NOT NULL,
    status     VARCHAR(10) NOT NULL CHECK (status IN ('FREE', 'BUSY')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_on TIMESTAMPTZ,
    CONSTRAINT fk_time_slots_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_time_slots_range CHECK (start_time < end_time)
);

-- Leading column (user_id) also serves as the FK index; the composite additionally
-- covers "slots for this user overlapping/near this time range" lookups used by
-- overlap validation and availability queries without a separate scan.
CREATE INDEX idx_time_slots_user_start_end ON time_slots (user_id, start_time, end_time);
