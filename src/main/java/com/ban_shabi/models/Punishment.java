package com.ban_shabi.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Punishment {
    private final String id;
    private final String playerName;
    private final UUID playerUuid;
    private final String ipAddress;
    private final PunishmentType type;
    private final String reason;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiry;
    private final String operator;
    private boolean active;
    private LocalDateTime removedAt;
    private String removedBy;

    public Punishment(String id, String playerName, UUID playerUuid, String ipAddress, PunishmentType type, 
                      String reason, LocalDateTime createdAt, LocalDateTime expiry, 
                      String operator, boolean active) {
        this.id = id;
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.ipAddress = ipAddress;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiry = expiry;
        this.operator = operator;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }

    public String getRemovedBy() {
        return removedBy;
    }

    public void setRemovedBy(String removedBy) {
        this.removedBy = removedBy;
    }
}
