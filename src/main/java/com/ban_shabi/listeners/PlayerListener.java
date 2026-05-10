package com.ban_shabi.listeners;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.time.format.DateTimeFormatter;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        String ipAddress = event.getAddress() != null ? event.getAddress().getHostAddress() : null;
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        Punishment ban = punishmentManager.getActivePunishment(playerName, PunishmentType.BAN);
        if (ban == null && ipAddress != null) {
            ban = punishmentManager.getActivePunishmentByIp(ipAddress, PunishmentType.BAN);
        }
        
        if (ban != null) {
            String dateStart = ban.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String kickMessage;
            if (ban.getExpiry() == null) {
                kickMessage = BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("ban.banned_message_permanent",
                    "id", ban.getId(), "reason", ban.getReason(), "operator", ban.getOperator(), 
                    "dateStart", dateStart));
            } else {
                String timeDisplay = PunishmentManager.formatDuration(
                    java.time.Duration.between(ban.getCreatedAt(), ban.getExpiry()).getSeconds());
                String remaining = PunishmentManager.formatRemainingTime(ban.getExpiry());
                kickMessage = BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("ban.banned_message",
                    "id", ban.getId(), "reason", ban.getReason(), "time", timeDisplay, 
                    "remaining", remaining, "operator", ban.getOperator(), "dateStart", dateStart));
            }
            
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String ipAddress = event.getPlayer().getAddress() != null ? 
                          event.getPlayer().getAddress().getAddress().getHostAddress() : null;
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        Punishment mute = punishmentManager.getActivePunishment(playerName, PunishmentType.MUTE);
        if (mute == null && ipAddress != null) {
            mute = punishmentManager.getActivePunishmentByIp(ipAddress, PunishmentType.MUTE);
        }
        
        if (mute != null) {
            event.setCancelled(true);
            String dateStart = mute.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String muteMessage;
            if (mute.getExpiry() == null) {
                muteMessage = BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("mute.muted_message_permanent",
                    "id", mute.getId(), "reason", mute.getReason(), "operator", mute.getOperator(), 
                    "dateStart", dateStart));
            } else {
                String timeDisplay = PunishmentManager.formatDuration(
                    java.time.Duration.between(mute.getCreatedAt(), mute.getExpiry()).getSeconds());
                String remaining = PunishmentManager.formatRemainingTime(mute.getExpiry());
                muteMessage = BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("mute.muted_message",
                    "id", mute.getId(), "reason", mute.getReason(), "time", timeDisplay, 
                    "remaining", remaining, "operator", mute.getOperator(), "dateStart", dateStart));
            }
            
            event.getPlayer().sendMessage(muteMessage);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String playerName = event.getPlayer().getName();
        String ipAddress = event.getPlayer().getAddress() != null ? 
                          event.getPlayer().getAddress().getAddress().getHostAddress() : null;
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        boolean isMuted = punishmentManager.isMuted(playerName);
        if (!isMuted && ipAddress != null) {
            isMuted = punishmentManager.isMutedByIp(ipAddress);
        }
        
        if (isMuted) {
            String command = event.getMessage().toLowerCase().split(" ")[0];
            
            if (command.equals("/tell") || command.equals("/msg") || command.equals("/me") ||
                command.equals("/minecraft:tell") || command.equals("/minecraft:msg") || command.equals("/minecraft:me")) {
                
                event.setCancelled(true);
                event.getPlayer().sendMessage(BanShabi.getInstance().getPrefix() + 
                    BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("mute.cannot_speak")));
            }
        }
    }
}
