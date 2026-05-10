package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmuteIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.unmuteip")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unmuteip.no_ip")));
            return true;
        }

        String ipAddress = args[0];
        PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
        
        Punishment muteToUnmute = punishmentManager.getActivePunishmentByIp(ipAddress, PunishmentType.MUTE);
        
        if (muteToUnmute == null) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unmuteip.not_muted", 
                             "ip", ipAddress)));
            return true;
        }

        String operator = sender instanceof Player ? ((Player) sender).getName() : "Console";
        punishmentManager.removePunishment(muteToUnmute.getId(), operator);
        sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                         BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("unmuteip.success", 
                         "ip", ipAddress)));

        return true;
    }
}
