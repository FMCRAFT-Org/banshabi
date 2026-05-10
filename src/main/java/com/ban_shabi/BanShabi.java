package com.ban_shabi;

import com.ban_shabi.commands.*;
import com.ban_shabi.listeners.PlayerListener;
import com.ban_shabi.managers.PunishmentManager;
import com.ban_shabi.managers.SmtpManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BanShabi extends JavaPlugin {

    private static BanShabi instance;
    private PunishmentManager punishmentManager;
    private SmtpManager smtpManager;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessages();

        punishmentManager = new PunishmentManager(this);
        smtpManager = new SmtpManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("BanShabi 插件已启用！");
    }

    @Override
    public void onDisable() {
        if (punishmentManager != null) {
            punishmentManager.saveData();
        }
        if (smtpManager != null) {
            smtpManager.shutdown();
        }
        getLogger().info("BanShabi 插件已禁用！");
    }

    private void registerCommands() {
        getCommand("ban").setExecutor(new BanCommand());
        getCommand("unban").setExecutor(new UnbanCommand());
        getCommand("kick").setExecutor(new KickCommand());
        getCommand("mute").setExecutor(new MuteCommand());
        getCommand("unmute").setExecutor(new UnmuteCommand());
        getCommand("warn").setExecutor(new WarnCommand());
        getCommand("unwarn").setExecutor(new UnwarnCommand());
        getCommand("ban-ip").setExecutor(new BanIpCommand());
        getCommand("unban-ip").setExecutor(new UnbanIpCommand());
        getCommand("mute-ip").setExecutor(new MuteIpCommand());
        getCommand("unmute-ip").setExecutor(new UnmuteIpCommand());
        getCommand("warn-ip").setExecutor(new WarnIpCommand());
        getCommand("unwarn-ip").setExecutor(new UnwarnIpCommand());
        getCommand("kick-ip").setExecutor(new KickIpCommand());
        getCommand("banlist").setExecutor(new BanlistCommand());
        getCommand("warnlist").setExecutor(new WarnlistCommand());
        getCommand("mutelist").setExecutor(new MutelistCommand());
        getCommand("lookip").setExecutor(new LookIpCommand());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "message.yml");
        if (!messagesFile.exists()) {
            saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        File messagesFile = new File(getDataFolder(), "message.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static BanShabi getInstance() {
        return instance;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public SmtpManager getSmtpManager() {
        return smtpManager;
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        return message != null ? message : path;
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
            }
        }
        return message;
    }

    public String getPrefix() {
        return getMessage("prefix").replace("&", "§");
    }

    public String colorize(String message) {
        return message.replace("&", "§");
    }
}
