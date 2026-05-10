package com.ban_shabi.managers;

import com.ban_shabi.BanShabi;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final BanShabi plugin;
    private final Map<String, Punishment> punishments;
    private final Map<String, List<Punishment>> playerPunishments;
    private final Map<String, List<Punishment>> ipPunishments;
    private int nextId;

    public PunishmentManager(BanShabi plugin) {
        this.plugin = plugin;
        this.punishments = new ConcurrentHashMap<>();
        this.playerPunishments = new ConcurrentHashMap<>();
        this.ipPunishments = new ConcurrentHashMap<>();
        loadData();
    }

    public Punishment addPunishment(String playerName, UUID playerUuid, String ipAddress, PunishmentType type, 
                                    String reason, long duration, String operator) {
        String id = String.valueOf(nextId++);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = duration > 0 ? now.plusSeconds(duration) : null;
        
        Punishment punishment = new Punishment(id, playerName, playerUuid, ipAddress, type, reason, 
                                               now, expiry, operator, true);
        
        punishments.put(id, punishment);
        playerPunishments.computeIfAbsent(playerName.toLowerCase(), k -> new ArrayList<>()).add(punishment);
        if (ipAddress != null && !ipAddress.isEmpty()) {
            ipPunishments.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(punishment);
        }

        saveData();

        SmtpManager smtpManager = plugin.getSmtpManager();
        if (smtpManager != null) {
            smtpManager.sendPunishmentEmail(punishment);
        }

        return punishment;
    }

    public boolean removePunishment(String id, String removedBy) {
        Punishment punishment = punishments.get(id);
        if (punishment != null && punishment.isActive()) {
            punishment.setActive(false);
            punishment.setRemovedAt(LocalDateTime.now());
            punishment.setRemovedBy(removedBy);
            List<Punishment> playerList = playerPunishments.get(punishment.getPlayerName().toLowerCase());
            if (playerList != null) {
                playerList.remove(punishment);
            }
            if (punishment.getIpAddress() != null) {
                List<Punishment> ipList = ipPunishments.get(punishment.getIpAddress());
                if (ipList != null) {
                    ipList.remove(punishment);
                }
            }
            saveData();
            return true;
        }
        return false;
    }

    public boolean removePunishment(String id) {
        return removePunishment(id, "Unknown");
    }

    public Punishment getPunishment(String id) {
        return punishments.get(id);
    }

    public List<Punishment> getPlayerPunishments(String playerName) {
        return playerPunishments.getOrDefault(playerName.toLowerCase(), new ArrayList<>());
    }

    public List<Punishment> getIpPunishments(String ipAddress) {
        return ipPunishments.getOrDefault(ipAddress, new ArrayList<>());
    }

    public List<Punishment> getActivePunishments(String playerName) {
        List<Punishment> active = new ArrayList<>();
        for (Punishment punishment : getPlayerPunishments(playerName)) {
            if (punishment.isActive() && !isExpired(punishment)) {
                active.add(punishment);
            }
        }
        return active;
    }

    public List<Punishment> getActivePunishmentsByIp(String ipAddress) {
        List<Punishment> active = new ArrayList<>();
        for (Punishment punishment : getIpPunishments(ipAddress)) {
            if (punishment.isActive() && !isExpired(punishment)) {
                active.add(punishment);
            }
        }
        return active;
    }

    public Punishment getActivePunishment(String playerName, PunishmentType type) {
        for (Punishment punishment : getActivePunishments(playerName)) {
            if (punishment.getType() == type) {
                return punishment;
            }
        }
        return null;
    }

    public Punishment getActivePunishmentByIp(String ipAddress, PunishmentType type) {
        for (Punishment punishment : getActivePunishmentsByIp(ipAddress)) {
            if (punishment.getType() == type) {
                return punishment;
            }
        }
        return null;
    }

    public boolean isBanned(String playerName) {
        Punishment ban = getActivePunishment(playerName, PunishmentType.BAN);
        return ban != null && !isExpired(ban);
    }

    public boolean isBannedByIp(String ipAddress) {
        Punishment ban = getActivePunishmentByIp(ipAddress, PunishmentType.BAN);
        return ban != null && !isExpired(ban);
    }

    public boolean isMuted(String playerName) {
        Punishment mute = getActivePunishment(playerName, PunishmentType.MUTE);
        return mute != null && !isExpired(mute);
    }

    public boolean isMutedByIp(String ipAddress) {
        Punishment mute = getActivePunishmentByIp(ipAddress, PunishmentType.MUTE);
        return mute != null && !isExpired(mute);
    }

    public boolean isExpired(Punishment punishment) {
        if (punishment.getExpiry() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(punishment.getExpiry());
    }

    public List<Punishment> getAllPunishments() {
        return new ArrayList<>(punishments.values());
    }

    public List<Punishment> getAllActivePunishments() {
        List<Punishment> active = new ArrayList<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment.isActive() && !isExpired(punishment)) {
                active.add(punishment);
            }
        }
        return active;
    }

    public List<Punishment> getPunishmentsByType(PunishmentType type) {
        List<Punishment> result = new ArrayList<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment.getType() == type) {
                result.add(punishment);
            }
        }
        return result;
    }

    public void cleanupExpiredPunishments() {
        List<String> expiredIds = new ArrayList<>();
        for (Map.Entry<String, Punishment> entry : punishments.entrySet()) {
            if (entry.getValue().isActive() && isExpired(entry.getValue())) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (String id : expiredIds) {
            Punishment punishment = punishments.get(id);
            punishment.setActive(false);
            List<Punishment> playerList = playerPunishments.get(punishment.getPlayerName().toLowerCase());
            if (playerList != null) {
                playerList.remove(punishment);
            }
            if (punishment.getIpAddress() != null) {
                List<Punishment> ipList = ipPunishments.get(punishment.getIpAddress());
                if (ipList != null) {
                    ipList.remove(punishment);
                }
            }
        }
        
        if (!expiredIds.isEmpty()) {
            saveData();
        }
    }

    private void loadData() {
        File dataFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!dataFile.exists()) {
            nextId = 1;
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        nextId = config.getInt("next_id", 1);

        ConfigurationSection punishmentsSection = config.getConfigurationSection("punishments");
        if (punishmentsSection != null) {
            for (String id : punishmentsSection.getKeys(false)) {
                ConfigurationSection punishmentSection = punishmentsSection.getConfigurationSection(id);
                if (punishmentSection != null) {
                    Punishment punishment = loadPunishment(id, punishmentSection);
                    punishments.put(id, punishment);
                    playerPunishments.computeIfAbsent(punishment.getPlayerName().toLowerCase(), 
                                                      k -> new ArrayList<>()).add(punishment);
                    if (punishment.getIpAddress() != null && !punishment.getIpAddress().isEmpty()) {
                        ipPunishments.computeIfAbsent(punishment.getIpAddress(), 
                                                      k -> new ArrayList<>()).add(punishment);
                    }
                }
            }
        }

        cleanupExpiredPunishments();
    }

    private Punishment loadPunishment(String id, ConfigurationSection section) {
        String playerName = section.getString("player_name");
        String uuidStr = section.getString("player_uuid");
        UUID playerUuid = uuidStr != null && !uuidStr.isEmpty() ? UUID.fromString(uuidStr) : null;
        String ipAddress = section.getString("ip_address", "");
        PunishmentType type = PunishmentType.valueOf(section.getString("type"));
        String reason = section.getString("reason");
        
        LocalDateTime createdAt = LocalDateTime.parse(section.getString("created_at"));
        LocalDateTime expiry = section.contains("expiry") ? 
                              LocalDateTime.parse(section.getString("expiry")) : null;
        
        String operator = section.getString("operator");
        boolean active = section.getBoolean("active", true);

        Punishment punishment = new Punishment(id, playerName, playerUuid, ipAddress, type, reason, 
                            createdAt, expiry, operator, active);
        
        if (section.contains("removed_at")) {
            punishment.setRemovedAt(LocalDateTime.parse(section.getString("removed_at")));
        }
        if (section.contains("removed_by")) {
            punishment.setRemovedBy(section.getString("removed_by"));
        }
        
        return punishment;
    }

    public void saveData() {
        File dataFile = new File(plugin.getDataFolder(), "punishments.yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("next_id", nextId);

        ConfigurationSection punishmentsSection = config.createSection("punishments");
        for (Map.Entry<String, Punishment> entry : punishments.entrySet()) {
            Punishment punishment = entry.getValue();
            ConfigurationSection punishmentSection = punishmentsSection.createSection(entry.getKey());
            
            punishmentSection.set("player_name", punishment.getPlayerName());
            punishmentSection.set("player_uuid", punishment.getPlayerUuid() != null ? punishment.getPlayerUuid().toString() : "");
            punishmentSection.set("ip_address", punishment.getIpAddress() != null ? punishment.getIpAddress() : "");
            punishmentSection.set("type", punishment.getType().name());
            punishmentSection.set("reason", punishment.getReason());
            punishmentSection.set("created_at", punishment.getCreatedAt().toString());
            if (punishment.getExpiry() != null) {
                punishmentSection.set("expiry", punishment.getExpiry().toString());
            }
            punishmentSection.set("operator", punishment.getOperator());
            punishmentSection.set("active", punishment.isActive());
            if (punishment.getRemovedAt() != null) {
                punishmentSection.set("removed_at", punishment.getRemovedAt().toString());
            }
            if (punishment.getRemovedBy() != null) {
                punishmentSection.set("removed_by", punishment.getRemovedBy());
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存处罚数据: " + e.getMessage());
        }
    }

    public static long parseDuration(String duration) {
        duration = duration.toLowerCase().trim();
        try {
            if (duration.equals("-1")) {
                return -1;
            } else if (duration.endsWith("d")) {
                return Long.parseLong(duration.substring(0, duration.length() - 1)) * 86400;
            } else if (duration.endsWith("h")) {
                return Long.parseLong(duration.substring(0, duration.length() - 1)) * 3600;
            } else if (duration.endsWith("m")) {
                return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60;
            } else if (duration.endsWith("s")) {
                return Long.parseLong(duration.substring(0, duration.length() - 1));
            } else {
                return -2;
            }
        } catch (NumberFormatException e) {
            return -2;
        }
    }

    public static String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "永久";
        }
        
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        if (minutes > 0) sb.append(minutes).append("分钟");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("秒");
        
        return sb.toString();
    }

    public static String formatRemainingTime(LocalDateTime expiry) {
        if (expiry == null) {
            return "永久";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiry)) {
            return "已过期";
        }
        
        long seconds = java.time.Duration.between(now, expiry).getSeconds();
        return formatDuration(seconds);
    }
}
