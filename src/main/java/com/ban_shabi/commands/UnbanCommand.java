package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.unban")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unban.player_not_found", 
                             "player", "未指定")));
            return true;
        }

        String input = args[0];
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        Punishment banToUnban = null;

        banToUnban = punishmentManager.getActivePunishment(input, PunishmentType.BAN);
        
        if (banToUnban == null && input.matches("\\d+")) {
            Punishment byId = punishmentManager.getPunishment(input);
            if (byId != null && byId.getType() == PunishmentType.BAN) {
                banToUnban = byId;
            }
        }
        
        if (banToUnban == null) {
            if (input.matches("\\d+")) {
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unban.id_not_found", 
                                 "id", input)));
            } else {
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unban.not_banned", 
                                 "player", input)));
            }
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        punishmentManager.removePunishment(banToUnban.getId(), operator);
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unban.success", 
                         "player", banToUnban.getPlayerName())));

        return true;
    }
}