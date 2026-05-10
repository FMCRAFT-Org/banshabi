package com.ban_shabi.managers;

import com.ban_shabi.BanShabi;
import com.ban_shabi.models.Punishment;
import com.ban_shabi.models.PunishmentType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmtpManager {

    private final BanShabi plugin;
    private FileConfiguration smtpConfig;
    private File configFile;
    private boolean enabled;
    private ExecutorService emailExecutor;

    public SmtpManager(BanShabi plugin) {
        this.plugin = plugin;
        this.emailExecutor = Executors.newSingleThreadExecutor();
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "smtp.yml");
        if (!configFile.exists()) {
            plugin.saveResource("smtp.yml", false);
        }
        smtpConfig = YamlConfiguration.loadConfiguration(configFile);
        enabled = smtpConfig.getBoolean("smtp.enabled", false);
        
        if (enabled) {
            plugin.getLogger().info("✅ SMTP 邮件通知已启用");
        } else {
            plugin.getLogger().info("ℹ️  SMTP 邮件通知未启用");
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<PunishmentType> getNotifyTypes() {
        List<PunishmentType> types = new ArrayList<>();
        List<String> typeStrings = smtpConfig.getStringList("notify-types");
        for (String typeStr : typeStrings) {
            try {
                types.add(PunishmentType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("⚠️ 无效的处罚类型: " + typeStr);
            }
        }
        return types;
    }

    public boolean shouldNotify(PunishmentType type) {
        return enabled && getNotifyTypes().contains(type);
    }

    public void sendPunishmentEmail(Punishment punishment) {
        if (!shouldNotify(punishment.getType())) {
            return;
        }

        emailExecutor.submit(() -> {
            try {
                String serverName = smtpConfig.getString("server.name", "Minecraft服务器");
                String subjectTemplate = smtpConfig.getString("email-template.subject");
                String htmlTemplate = smtpConfig.getString("email-template.html");

                String punishmentTypeDisplay = getPunishmentTypeDisplayName(punishment.getType());
                String durationStr = PunishmentManager.formatDuration(
                    punishment.getExpiry() != null ? 
                    java.time.Duration.between(punishment.getCreatedAt(), punishment.getExpiry()).getSeconds() : -1
                );
                String timestamp = punishment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                String subject = subjectTemplate
                    .replace("{server_name}", serverName)
                    .replace("{player_name}", punishment.getPlayerName())
                    .replace("{punishment_type}", punishmentTypeDisplay);

                String htmlContent = htmlTemplate
                    .replace("{player_name}", punishment.getPlayerName())
                    .replace("{server_name}", serverName)
                    .replace("{reason}", punishment.getReason())
                    .replace("{operator}", punishment.getOperator())
                    .replace("{punishment_type}", punishment.getType().name())
                    .replace("{punishment_type_display}", punishmentTypeDisplay)
                    .replace("{duration}", durationStr)
                    .replace("{timestamp}", timestamp);

                List<String> recipients = smtpConfig.getStringList("recipients");
                
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpConfig.getString("smtp.host"));
                props.put("mail.smtp.port", smtpConfig.getInt("smtp.port", 587));
                props.put("mail.smtp.auth", smtpConfig.getBoolean("smtp.use-auth", true));
                props.put("mail.smtp.starttls.enable", smtpConfig.getBoolean("smtp.use-tls", true));

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            smtpConfig.getString("smtp.username"),
                            smtpConfig.getString("smtp.password")
                        );
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(
                    smtpConfig.getString("smtp.from-address"),
                    smtpConfig.getString("smtp.from-name", "BanShabi"),
                    "UTF-8"
                ));

                for (String recipient : recipients) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                }

                message.setSubject(subject, "UTF-8");
                message.setContent(htmlContent, "text/html; charset=UTF-8");

                Transport.send(message);
                plugin.getLogger().info("📧 处罚邮件已发送: " + punishment.getPlayerName() + 
                                       " (" + punishmentTypeDisplay + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("❌ 发送处罚邮件失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getPunishmentTypeDisplayName(PunishmentType type) {
        switch (type) {
            case BAN: return "封禁";
            case MUTE: return "禁言";
            case WARN: return "警告";
            case KICK: return "踢出";
            default: return type.name();
        }
    }

    public void shutdown() {
        if (emailExecutor != null && !emailExecutor.isShutdown()) {
            emailExecutor.shutdown();
        }
    }
}
