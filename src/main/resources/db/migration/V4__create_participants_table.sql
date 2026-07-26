CREATE TABLE participants
(
    id           UUID PRIMARY KEY,
    meeting_id   UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    role         VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'PARTICIPANT')),
    status       VARCHAR(20) NOT NULL CHECK (status IN ('INVITED', 'ACCEPTED', 'DECLINED')),
    responded_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL,
    -- Participants are children of the Meeting aggregate: cascade so deleting a
    -- meeting (i.e. cancelling it) removes its participants at the DB level too,
    -- as a safety net alongside JPA's own cascade/orphanRemoval on Meeting.participants.
    CONSTRAINT fk_participants_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_participants_meeting_user UNIQUE (meeting_id, user_id)
);

-- meeting_id is already covered by the leading column of uq_participants_meeting_user.
CREATE INDEX idx_participants_user ON participants (user_id);
