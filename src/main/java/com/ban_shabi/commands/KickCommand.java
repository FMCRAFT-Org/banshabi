package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KickCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.kick")) {
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
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kick.no_reason")));
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
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kick.no_reason")));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kick.player_not_found", 
                             "player", playerName)));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("kick.success", 
                         "player", playerName)));

        if (!silent) {
            Bukkit.broadcastMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("broadcast.punishment",
                "player", playerName, "reason", reasonStr, "operator", operator, 
                "type", "踢出", "time", "-")));
        }

        String kickMessage = BanShabi.getInstance().colorize(
            BanShabi.getInstance().getMessage("kick.kicked_message",
            "reason", reasonStr, "operator", operator));
        
        targetPlayer.kickPlayer(kickMessage);

        return true;
    }
}
