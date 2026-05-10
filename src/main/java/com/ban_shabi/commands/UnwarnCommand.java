package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnwarnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.unwarn")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarn.player_not_found", 
                             "player", "未指定")));
            return true;
        }

        String input = args[0];
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        Punishment warnToRemove = null;

        java.util.List<Punishment> warnings = punishmentManager.getPlayerPunishments(input);
        java.util.List<Punishment> activeWarnings = new java.util.ArrayList<>();
        
        for (Punishment warning : warnings) {
            if (warning.getType() == PunishmentType.WARN && warning.isActive()) {
                activeWarnings.add(warning);
            }
        }
        
        if (!activeWarnings.isEmpty()) {
            warnToRemove = activeWarnings.get(activeWarnings.size() - 1);
        } else if (input.matches("\\d+")) {
            Punishment byId = punishmentManager.getPunishment(input);
            if (byId != null && byId.getType() == PunishmentType.WARN) {
                warnToRemove = byId;
            }
        }
        
        if (warnToRemove == null) {
            if (input.matches("\\d+")) {
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarn.id_not_found", 
                                 "id", input)));
            } else {
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarn.no_warnings", 
                                 "player", input)));
            }
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        punishmentManager.removePunishment(warnToRemove.getId(), operator);
        
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarn.success", 
                         "player", warnToRemove.getPlayerName())));

        return true;
    }
}