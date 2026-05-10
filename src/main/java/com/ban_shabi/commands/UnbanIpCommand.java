package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.unbanip")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unbanip.no_ip")));
            return true;
        }

        String ipAddress = args[0];
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        Punishment banToUnban = punishmentManager.getActivePunishmentByIp(ipAddress, PunishmentType.BAN);
        
        if (banToUnban == null) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unbanip.not_banned", 
                             "ip", ipAddress)));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        punishmentManager.removePunishment(banToUnban.getId(), operator);
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unbanip.success", 
                         "ip", ipAddress)));

        return true;
    }
}
