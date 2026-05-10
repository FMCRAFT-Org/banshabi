package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BanlistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.banlist")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();

        if (args.length > 0) {
            String playerName = args[0];
            List<Punishment> playerBans = punishmentManager.getPlayerPunishments(playerName);
            
            sender.sendMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("banlist.player_header", "player", playerName)));
            
            boolean found = false;
            for (Punishment ban : playerBans) {
                if (ban.getType() == PunishmentType.BAN) {
                    found = true;
                    String entry;
                    if (!ban.isActive()) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("banlist.entry_removed",
                            "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                    } else if (ban.getExpiry() == null) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("banlist.entry_permanent",
                            "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                    } else if (punishmentManager.isExpired(ban)) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("banlist.entry_expired",
                            "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                    } else {
                        String remaining = PunishmentManager.formatRemainingTime(ban.getExpiry());
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("banlist.entry",
                            "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason(), 
                            "remaining", remaining));
                    }
                    sender.sendMessage(entry);
                }
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("banlist.player_no_bans", "player", playerName)));
            }
        } else {
            List<Punishment> allBans = punishmentManager.getPunishmentsByType(PunishmentType.BAN);
            
            sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banlist.header")));
            
            boolean found = false;
            for (Punishment ban : allBans) {
                String entry;
                if (!ban.isActive()) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banlist.entry_removed",
                        "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                } else if (ban.getExpiry() == null) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banlist.entry_permanent",
                        "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                } else if (punishmentManager.isExpired(ban)) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banlist.entry_expired",
                        "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason()));
                } else {
                    found = true;
                    String remaining = PunishmentManager.formatRemainingTime(ban.getExpiry());
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("banlist.entry",
                        "id", ban.getId(), "player", ban.getPlayerName(), "reason", ban.getReason(), 
                        "remaining", remaining));
                }
                sender.sendMessage(entry);
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banlist.no_bans")));
            }
        }

        sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("banlist.footer")));
        return true;
    }
}