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

public class WarnIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.warnip")) {
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
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnip.no_reason")));
            return true;
        }

        String ipAddress = filteredArgs[0];
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < filteredArgs.length; i++) {
            reason.append(filteredArgs[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        if (reasonStr.isEmpty()) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnip.no_reason")));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        Punishment punishment = BanShabi.getInstance().getPunishmentManager()
            .addPunishment("IP:" + ipAddress, null, ipAddress, PunishmentType.WARN, reasonStr, 0, operator);

        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnip.success", 
                         "ip", ipAddress)));

        if (!silent) {
            Bukkit.broadcastMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("broadcast.punishment",
                "player", "IP:" + ipAddress, "reason", reasonStr, "operator", operator, 
                "type", "IP警告", "time", "-")));
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String playerIp = onlinePlayer.getAddress() != null ? onlinePlayer.getAddress().getAddress().getHostAddress() : null;
            if (playerIp != null && playerIp.equals(ipAddress)) {
                String dateStart = punishment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String warnMessage = BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("warnip.warned_message",
                    "id", punishment.getId(), "reason", reasonStr, "operator", operator, 
                    "dateStart", dateStart, "ip", ipAddress));
                onlinePlayer.sendMessage(warnMessage);
            }
        }

        return true;
    }
}
