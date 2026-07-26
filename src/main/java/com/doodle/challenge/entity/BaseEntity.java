package com.doodle.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity<T extends BaseEntity<T>> extends AbstractAggregateRoot<T> {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BaseEntity() {
        // required by JPA
    }

    protected BaseEntity(UUID id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    protected void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // widened to public so same-package tests can assert on registered events without a repository save
    @Override
    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseEntity<?> other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
