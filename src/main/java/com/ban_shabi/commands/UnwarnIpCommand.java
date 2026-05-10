package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnwarnIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.unwarnip")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarnip.no_ip")));
            return true;
        }

        String ipAddress = args[0];
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        List<Punishment> ipPunishments = punishmentManager.getIpPunishments(ipAddress);
        List<Punishment> activeWarnings = new ArrayList<>();
        
        for (Punishment punishment : ipPunishments) {
            if (punishment.getType() == PunishmentType.WARN && punishment.isActive()) {
                activeWarnings.add(punishment);
            }
        }
        
        if (activeWarnings.isEmpty()) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarnip.no_warnings", 
                             "ip", ipAddress)));
            return true;
        }

        Punishment warnToRemove = activeWarnings.get(activeWarnings.size() - 1);
        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        punishmentManager.removePunishment(warnToRemove.getId(), operator);
        
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unwarnip.success", 
                         "ip", ipAddress)));

        return true;
    }
}
