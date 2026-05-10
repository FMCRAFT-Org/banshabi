package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KickIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.kickip")) {
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
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kickip.no_reason")));
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
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kickip.no_reason")));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kickip.success", 
                         "ip", ipAddress)));

        if (!silent) {
            Bukkit.broadcastMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("broadcast.punishment",
                "player", "IP:" + ipAddress, "reason", reasonStr, "operator", operator, 
                "type", "IP踢出", "time", "-")));
        }

        String kickMessage = BanShabi.getInstance().colorize(
            BanShabi.getInstance().getMessage("kickip.kicked_message",
            "reason", reasonStr, "operator", operator, "ip", ipAddress));
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String playerIp = onlinePlayer.getAddress() != null ? onlinePlayer.getAddress().getAddress().getHostAddress() : null;
            if (playerIp != null && playerIp.equals(ipAddress)) {
                onlinePlayer.kickPlayer(kickMessage);
            }
        }

        return true;
    }
}
