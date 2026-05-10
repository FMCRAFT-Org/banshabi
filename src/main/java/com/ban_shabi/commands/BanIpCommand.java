package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BanIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.banip")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        boolean silent = false;
        List<String> argsList = new ArrayList<>();
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                argsList.add(arg);
            }
        }
        
        String[] filteredArgs = argsList.toArray(new String[0]);

        if (filteredArgs.length < 3) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banip.invalid_time")));
            return true;
        }

        String ipAddress = filteredArgs[0];
        String timeStr = filteredArgs[1];
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < filteredArgs.length; i++) {
            reason.append(filteredArgs[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        if (reasonStr.isEmpty()) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banip.no_reason")));
            return true;
        }

        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        if (punishmentManager.isBannedByIp(ipAddress)) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banip.already_banned", 
                             "ip", ipAddress)));
            return true;
        }

        long duration = PunishmentManager.parseDuration(timeStr);
        if (duration == -2) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banip.invalid_time")));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        Punishment punishment = punishmentManager.addPunishment("IP:" + ipAddress, null, ipAddress, PunishmentType.BAN, 
                                                                 reasonStr, duration, operator);

        String timeDisplay = duration <= 0 ? "永久" : PunishmentManager.formatDuration(duration);
        
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banip.success", 
                         "ip", ipAddress)));

        if (!silent) {
            Bukkit.broadcastMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("broadcast.punishment",
                "player", "IP:" + ipAddress, "reason", reasonStr, "operator", operator, 
                "type", "IP封禁", "time", timeDisplay)));
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String playerIp = onlinePlayer.getAddress() != null ? onlinePlayer.getAddress().getAddress().getHostAddress() : null;
            if (playerIp != null && playerIp.equals(ipAddress)) {
                String dateStart = punishment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String kickMessage;
                if (duration <= 0) {
                    kickMessage = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banip.banned_message_permanent",
                        "id", punishment.getId(), "reason", reasonStr, "operator", operator, 
                        "dateStart", dateStart, "ip", ipAddress));
                } else {
                    String remaining = PunishmentManager.formatRemainingTime(punishment.getExpiry());
                    kickMessage = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banip.banned_message",
                        "id", punishment.getId(), "reason", reasonStr, "time", timeDisplay, 
                        "remaining", remaining, "operator", operator, "dateStart", dateStart, "ip", ipAddress));
                }
                onlinePlayer.kickPlayer(kickMessage);
            }
        }

        return true;
    }
}
