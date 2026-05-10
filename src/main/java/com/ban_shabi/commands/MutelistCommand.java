package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MutelistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.mutelist")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();

        if (args.length > 0) {
            String playerName = args[0];
            List<Punishment> playerMutes = punishmentManager.getPlayerPunishments(playerName);
            
            sender.sendMessage(BanShabi.getInstance().colorize(
                BanShabi.getInstance().getMessage("mutelist.player_header", "player", playerName)));
            
            boolean found = false;
            for (Punishment mute : playerMutes) {
                if (mute.getType() == PunishmentType.MUTE) {
                    found = true;
                    String entry;
                    if (!mute.isActive()) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("mutelist.entry_removed",
                            "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                    } else if (mute.getExpiry() == null) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("mutelist.entry_permanent",
                            "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                    } else if (punishmentManager.isExpired(mute)) {
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("mutelist.entry_expired",
                            "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                    } else {
                        String remaining = PunishmentManager.formatRemainingTime(mute.getExpiry());
                        entry = BanShabi.getInstance().colorize(
                            BanShabi.getInstance().getMessage("mutelist.entry",
                            "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason(), 
                            "remaining", remaining));
                    }
                    sender.sendMessage(entry);
                }
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(
                    BanShabi.getInstance().getMessage("mutelist.player_no_mutes", "player", playerName)));
            }
        } else {
            List<Punishment> allMutes = punishmentManager.getPunishmentsByType(PunishmentType.MUTE);
            
            sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("mutelist.header")));
            
            boolean found = false;
            for (Punishment mute : allMutes) {
                String entry;
                if (!mute.isActive()) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("mutelist.entry_removed",
                        "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                } else if (mute.getExpiry() == null) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("mutelist.entry_permanent",
                        "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                } else if (punishmentManager.isExpired(mute)) {
                    found = true;
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("mutelist.entry_expired",
                        "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason()));
                } else {
                    found = true;
                    String remaining = PunishmentManager.formatRemainingTime(mute.getExpiry());
                    entry = BanShabi.getInstance().colorize(
                        BanShabi.getInstance().getMessage("mutelist.entry",
                        "id", mute.getId(), "player", mute.getPlayerName(), "reason", mute.getReason(), 
                        "remaining", remaining));
                }
                sender.sendMessage(entry);
            }
            
            if (!found) {
                sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("mutelist.no_mutes")));
            }
        }

        sender.sendMessage(BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("mutelist.footer")));
        return true;
    }
}
