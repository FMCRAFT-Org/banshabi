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
import java.util.UUID;

public class WarnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.warn")) {
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

        if (filteredArgs.length < 2) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warn.no_reason")));
            return true;
        }

        String playerName = filteredArgs[0];
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < filteredArgs.length; i++) {
            reason.append(filteredArgs[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        if (reasonStr.isEmpty()) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warn.no_reason")));
            return true;
        }

        UUID playerUuid = null;
        String ipAddress = null;
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer != null) {
            playerUuid = targetPlayer.getUniqueId();
            ipAddress = targetPlayer.getAddress() != null ? targetPlayer.getAddress().getAddress().getHostAddress() : null;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        Punishment punishment = BanShabi.getInstance().getPunishmentManager()
            .addPunishment(playerName, playerUuid, ipAddress, PunishmentType.WARN, reasonStr, 0, operator);

        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warn.success", 
                         "player", playerName)));

        if (!silent) {
            Bukkit.broadcastMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("broadcast.punishment",
                "player", playerName, "reason", reasonStr, "operator", operator, 
                "type", "警告", "time", "-")));
        }

        if (targetPlayer != null) {
            int warningCount = BanShabi.getInstance().getPunishmentManager()
                .getPlayerPunishments(playerName).size();
            
            String dateStart = punishment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String warnMessage = BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("warn.warned_message",
                "id", punishment.getId(), "reason", reasonStr, "operator", operator, 
                "dateStart", dateStart));
            
            String countMessage = BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("warn.warning_count", "count", String.valueOf(warningCount)));
            
            targetPlayer.sendMessage(warnMessage);
            targetPlayer.sendMessage(countMessage);
        }

        return true;
    }
}
