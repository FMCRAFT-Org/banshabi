package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarnlistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.warnlist")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (args.length > 0) {
            String playerName = args[0];
            List<Punishment> playerWarnings = punishmentManager.getPlayerPunishments(playerName);
            
            sender.sendMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("warnlist.player_header", "player", playerName)));
            
            boolean found = false;
            for (Punishment warning : playerWarnings) {
                if (warning.getType() == PunishmentType.WARN) {
                    found = true;
                    String date = warning.getCreatedAt().format(formatter);
                    String entry;
                    if (!warning.isActive()) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("warnlist.entry_removed",
                            "id", warning.getId(), "player", warning.getPlayerName(), 
                            "reason", warning.getReason(), "date", date));
                    } else {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("warnlist.entry",
                            "id", warning.getId(), "player", warning.getPlayerName(), 
                            "reason", warning.getReason(), "date", date));
                    }
                    sender.sendMessage(entry);
                }
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("warnlist.player_no_warnings", "player", playerName)));
            }
        } else {
            List<Punishment> allWarnings = punishmentManager.getPunishmentsByType(PunishmentType.WARN);
            
            sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnlist.header")));
            
            boolean found = false;
            for (Punishment warning : allWarnings) {
                String date = warning.getCreatedAt().format(formatter);
                String entry;
                if (!warning.isActive()) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("warnlist.entry_removed",
                        "id", warning.getId(), "player", warning.getPlayerName(), 
                        "reason", warning.getReason(), "date", date));
                } else {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("warnlist.entry",
                        "id", warning.getId(), "player", warning.getPlayerName(), 
                        "reason", warning.getReason(), "date", date));
                }
                sender.sendMessage(entry);
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnlist.no_warnings")));
            }
        }

        sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("warnlist.footer")));
        return true;
    }
}