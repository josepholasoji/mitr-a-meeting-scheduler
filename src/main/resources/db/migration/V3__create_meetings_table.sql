CREATE TABLE meetings
(
    id           UUID PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    owner_id     UUID         NOT NULL,
    time_slot_id UUID         NOT NULL,
    start_time   TIMESTAMPTZ  NOT NULL,
    end_time     TIMESTAMPTZ  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_meetings_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_meetings_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slots (id),
    -- A slot backs at most one meeting at a time (it's marked BUSY for the duration).
    CONSTRAINT uq_meetings_time_slot UNIQUE (time_slot_id),
    CONSTRAINT chk_meetings_range CHECK (start_time < end_time)
);

CREATE INDEX idx_meetings_owner ON meetings (owner_id);
-- time_slot_id is already indexed by the uq_meetings_time_slot unique constraint above.
