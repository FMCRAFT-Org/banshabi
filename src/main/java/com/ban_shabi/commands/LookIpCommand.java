package com.ban_shabi.commands;

import com.ban_shabi.BanShabi;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LookIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banshabi.lookip")) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("lookip.no_player")));
            return true;
        }

        String playerName = args[0];
        
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        
        if (targetPlayer != null && targetPlayer.getAddress() != null) {
            String ipAddress = targetPlayer.getAddress().getAddress().getHostAddress();
            sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                             BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("lookip.online",
                             "player", playerName, "ip", ipAddress)));
        } else {
            PunishmentManager punishmentManager = BanShabi.getInstance().getPunishmentManager();
            List<Punishment> punishments = punishmentManager.getPlayerPunishments(playerName);
            
            Set<String> ipAddresses = new HashSet<>();
            for (Punishment punishment : punishments) {
                if (punishment.getIpAddress() != null && !punishment.getIpAddress().isEmpty()) {
                    ipAddresses.add(punishment.getIpAddress());
                }
            }
            
            if (ipAddresses.isEmpty()) {
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("lookip.no_record",
                                 "player", playerName)));
            } else {
                String ipList = String.join(", ", ipAddresses);
                sender.sendMessage(BanShabi.getInstance().getPrefix() + 
                                 BanShabi.getInstance().colorize(BanShabi.getInstance().getMessage("lookip.offline",
                                 "player", playerName, "ip", ipList)));
            }
        }

        return true;
    }
}
